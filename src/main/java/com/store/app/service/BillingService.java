package com.store.app.service;

import com.store.app.dto.*;
import com.store.app.entity.*;
import com.store.app.enums.LedgerType;
import com.store.app.enums.PaymentType;
import com.store.app.enums.ReferenceType;
import com.store.app.enums.StockTxnType;
import com.store.app.repository.*;
import com.store.app.util.WhatsAppTemplates;
import jakarta.transaction.Transactional;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class BillingService {

    private final ItemRepository itemRepo;
    private final StockRepository stockRepo;
    private final BillRepository billRepo;

    @Getter
    private final BillItemRepository billItemRepo;

    private final StockTransactionRepository stockTxnRepo;
    private final CustomerRepository customerRepo;
    private final CustomerLedgerRepository ledgerRepo;
    private final CustomerService customerService;
    private final ValidationService validationService;
    private final AuthService authService;
    private final AuditService auditService;

    @Getter
    private final NotificationService notificationService;

    public BillingService(
            BillRepository billRepo,
            ItemRepository itemRepo,
            StockRepository stockRepo,
            BillItemRepository billItemRepo,
            StockTransactionRepository stockTxnRepo,
            CustomerRepository customerRepo,
            CustomerLedgerRepository ledgerRepo,
            CustomerService customerService,
            ValidationService validationService,
            AuthService authService,
            AuditService auditService,
            NotificationService notificationService
    ) {
        this.billRepo = billRepo;
        this.itemRepo = itemRepo;
        this.stockRepo = stockRepo;
        this.billItemRepo = billItemRepo;
        this.stockTxnRepo = stockTxnRepo;
        this.customerRepo = customerRepo;
        this.ledgerRepo = ledgerRepo;
        this.customerService = customerService;
        this.validationService = validationService;
        this.authService = authService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    /* =====================================================
       CREATE BILL — CREDIT CUSTOMER FIX
       ===================================================== */
    @Transactional
    public Bill createBill(CreateBillRequest req, User user) {

        authService.requireBillingOrOwner(user);
        validationService.validateBillItems(req.getItems());

        Customer customer = null;

        if (req.getCustomerId() != null) {
            customer = customerRepo.findById(req.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
        }

        // 🔒 CREDIT requires customer
        if (req.getPaymentType() == PaymentType.CREDIT) {

            if (customer == null) {

                if (req.getCustomerName() == null || req.getCustomerName().isBlank()) {
                    throw new RuntimeException("CREDIT bill requires customer name");
                }

                if (req.getCustomerMobile() == null || req.getCustomerMobile().isBlank()) {
                    throw new RuntimeException("CREDIT bill requires customer mobile");
                }

                customer = customerService.createCustomer(
                        req.getCustomerName().trim(),
                        req.getCustomerMobile().trim(),
                        req.getCustomerAddress()
                );
            }
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (BillItemRequest i : req.getItems()) {
            subtotal = subtotal.add(i.getQuantity().multiply(i.getPrice()));
        }

        BigDecimal discount =
                req.getDiscountAmount() != null ? req.getDiscountAmount() : BigDecimal.ZERO;

        if (discount.signum() < 0 || discount.compareTo(subtotal) > 0) {
            throw new RuntimeException("Invalid discount");
        }

        BigDecimal finalAmount = subtotal.subtract(discount);

        BigDecimal amountPaid =
                req.getAmountPaid() != null
                        ? req.getAmountPaid()
                        : defaultPaidAmount(req.getPaymentType(), finalAmount);

        if (amountPaid.signum() < 0 || amountPaid.compareTo(finalAmount) > 0) {
            throw new RuntimeException("Invalid paid amount");
        }

        BigDecimal dueAmount = finalAmount.subtract(amountPaid);

        Bill bill = new Bill();
        bill.setBillNumber(generateBillNumber());
        bill.setBillCode(generateBillCode());
        bill.setPaymentType(req.getPaymentType());
        bill.setCustomer(customer);
        bill.setCreatedBy(user);
        bill.setSubtotal(subtotal);
        bill.setDiscountAmount(discount);
        bill.setTotalAmount(finalAmount);

        billRepo.save(bill);

        for (BillItemRequest itemReq : req.getItems()) {

            Item item = itemRepo.findById(itemReq.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            validationService.validateQuantity(item.getBaseUnit(), itemReq.getQuantity());
            validationService.validatePrice(itemReq.getPrice(), item.getCostPrice());

            Stock stock = stockRepo.findById(item.getId())
                    .orElseThrow(() -> new RuntimeException("Stock not found"));

            BigDecimal fulfilled =
                    stock.getQuantity().min(itemReq.getQuantity());

            BigDecimal pending =
                    itemReq.getQuantity().subtract(fulfilled);

            BillItem bi = new BillItem();
            bi.setBill(bill);
            bi.setItem(item);
            bi.setQuantity(itemReq.getQuantity());
            bi.setPrice(itemReq.getPrice());
            bi.setAmount(itemReq.getPrice().multiply(itemReq.getQuantity()));
            bi.setFulfilledQty(fulfilled);
            bi.setPendingQty(pending);

            bi.setFulfilmentStatus(
                    pending.signum() == 0 ? "FULL"
                            : fulfilled.signum() == 0 ? "PENDING"
                            : "PARTIAL"
            );

            bill.getItems().add(bi);

            if (fulfilled.signum() > 0) {
                stock.setQuantity(stock.getQuantity().subtract(fulfilled));
                stockRepo.save(stock);

                StockTransaction txn = new StockTransaction();
                txn.setItem(item);
                txn.setTransactionType(StockTxnType.OUT);
                txn.setQuantity(fulfilled);
                txn.setReferenceType(ReferenceType.BILL);
                txn.setReferenceId(bill.getId());
                stockTxnRepo.save(txn);
            }
        }

        if (bill.getPaymentType() == PaymentType.CREDIT && dueAmount.signum() > 0) {
            CustomerLedger debit = new CustomerLedger();
            debit.setCustomer(customer);
            debit.setBill(bill);
            debit.setEntryType(LedgerType.DEBIT);
            debit.setAmount(dueAmount);
            ledgerRepo.save(debit);
        }

        auditService.log(
                "BILL",
                bill.getId(),
                "CREATE",
                null,
                finalAmount.toString(),
                user
        );

        if (customer != null && customer.getMobile() != null) {
            notificationService.sendWhatsApp(
                    customer.getMobile(),
                    WhatsAppTemplates.billCreated(bill)
            );
        }

        return bill;
    }

    /* =====================================================
       READ — BILL DETAILS
       ===================================================== */
    public BillResponse getBillById(UUID billId) {

        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        BigDecimal due = ledgerRepo.getDueForBill(billId);
        BigDecimal paid = bill.getTotalAmount().subtract(due);

        BillResponse res = new BillResponse();
        res.setBillId(bill.getId());
        res.setBillNumber(bill.getBillNumber());
        res.setBillCode(bill.getBillCode());
        res.setBillDate(bill.getCreatedAt());
        res.setPaymentType(bill.getPaymentType().name());

        if (bill.getCustomer() != null) {
            res.setCustomerName(bill.getCustomer().getName());
            res.setCustomerMobile(bill.getCustomer().getMobile());
            res.setCustomerAddress(bill.getCustomer().getAddress());
            res.setCustomerCode(bill.getCustomer().getCustomerCode());
        }

        res.setSubtotal(bill.getSubtotal());
        res.setDiscountAmount(bill.getDiscountAmount());
        res.setTotalAmount(bill.getTotalAmount());
        res.setAmountPaid(paid);
        res.setDueAmount(due);

        return res;
    }

    /* =====================================================
       PRINT BILL
       ===================================================== */
    public BillPrintResponse getBillForPrint(UUID billId) {

        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        BigDecimal due = ledgerRepo.getDueForBill(billId);
        BigDecimal paid = bill.getTotalAmount().subtract(due);

        return new BillPrintResponse(
                bill.getBillNumber(),
                bill.getBillCode(),
                bill.getCreatedAt(),
                bill.getPaymentType().name(),
                bill.getCustomer() != null ? bill.getCustomer().getName() : null,
                bill.getCustomer() != null ? bill.getCustomer().getMobile() : null,
                bill.getCustomer() != null ? bill.getCustomer().getAddress() : null,
                bill.getItems().stream()
                        .map(i -> new BillPrintResponse.Item(
                                i.getItem().getName(),
                                i.getQuantity(),
                                i.getItem().getBaseUnit().name(),
                                i.getPrice(),
                                i.getAmount()
                        ))
                        .toList(),
                bill.getSubtotal(),
                bill.getDiscountAmount(),
                bill.getTotalAmount(),
                paid,
                due
        );
    }

    /* =====================================================
       SETTLE BILL
       ===================================================== */
    @Transactional
    public void settleBill(UUID billId, SettleBillRequest req, User user) {

        authService.requireBillingOrOwner(user);

        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        BigDecimal due = ledgerRepo.getDueForBill(billId);

        BigDecimal paid = req.getAmountPaid() != null ? req.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal adjustment = req.getAdjustment() != null ? req.getAdjustment() : BigDecimal.ZERO;

        if (paid.add(adjustment).compareTo(due) > 0) {
            throw new RuntimeException("Settlement exceeds due");
        }

        if (paid.signum() > 0) {
            CustomerLedger credit = new CustomerLedger();
            credit.setCustomer(bill.getCustomer());
            credit.setBill(bill);
            credit.setEntryType(LedgerType.CREDIT);
            credit.setAmount(paid);
            ledgerRepo.save(credit);
        }

        if (adjustment.signum() > 0) {
            CustomerLedger waive = new CustomerLedger();
            waive.setCustomer(bill.getCustomer());
            waive.setBill(bill);
            waive.setEntryType(LedgerType.ADJUSTMENT);
            waive.setAmount(adjustment);
            ledgerRepo.save(waive);
        }
    }

    /* =====================================================
       HELPERS
       ===================================================== */
    private BigDecimal defaultPaidAmount(PaymentType type, BigDecimal total) {
        return type == PaymentType.CREDIT ? BigDecimal.ZERO : total;
    }

    private String generateBillNumber() {
        return "BILL-" + String.format("%04d", billRepo.count() + 1);
    }

    private String generateBillCode() {

        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        long countToday =
                billRepo.countByCreatedAtBetween(start, end) + 1;

        return "BILL-" +
                today.format(DateTimeFormatter.BASIC_ISO_DATE) +
                String.format("%02d", countToday);
    }
}
