package com.store.app.service;

import com.store.app.dto.BillItemRequest;
import com.store.app.dto.CreateBillRequest;
import com.store.app.entity.*;
import com.store.app.enums.PaymentType;
import com.store.app.enums.ReferenceType;
import com.store.app.enums.StockTxnType;
import com.store.app.repository.*;
import com.store.app.util.WhatsAppTemplates;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BillingService {

    private final ItemRepository itemRepo;
    private final StockRepository stockRepo;
    private final BillRepository billRepo;
    private final BillItemRepository billItemRepo;
    private final StockTransactionRepository stockTxnRepo;
    private final CustomerRepository customerRepo;
    private final CustomerLedgerRepository ledgerRepo;
    private final ValidationService validationService;
    private final AuthService authService;
    private final AuditService auditService;
    private final NotificationService notificationService; // 🆕

    public BillingService(
            BillRepository billRepo,
            ItemRepository itemRepo,
            StockRepository stockRepo,
            BillItemRepository billItemRepo,
            StockTransactionRepository stockTxnRepo,
            CustomerRepository customerRepo,
            CustomerLedgerRepository ledgerRepo,
            ValidationService validationService,
            AuthService authService,
            AuditService auditService,
            NotificationService notificationService // 🆕
    ) {
        this.billRepo = billRepo;
        this.itemRepo = itemRepo;
        this.stockRepo = stockRepo;
        this.billItemRepo = billItemRepo;
        this.stockTxnRepo = stockTxnRepo;
        this.customerRepo = customerRepo;
        this.ledgerRepo = ledgerRepo;
        this.validationService = validationService;
        this.authService = authService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Bill createBill(CreateBillRequest request, User user) {

        authService.requireBillingOrOwner(user);

        Bill bill = new Bill();
        bill.setBillNumber(generateBillNumber());
        bill.setBillCode(generateBillCode());
        bill.setPaymentType(request.getPaymentType());
        bill.setCreatedBy(user);
        bill.setTotalAmount(BigDecimal.ZERO);

        if (request.getCustomerId() != null) {
            Customer customer = customerRepo.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            bill.setCustomer(customer);
        }

        bill = billRepo.save(bill);

        BigDecimal total = BigDecimal.ZERO;

        for (BillItemRequest itemReq : request.getItems()) {

            Item item = itemRepo.findById(itemReq.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            validationService.validateQuantity(
                    item.getBaseUnit(),
                    itemReq.getQuantity()
            );

            validationService.validatePrice(
                    itemReq.getPrice(),
                    item.getCostPrice()
            );

            Stock stock = stockRepo.findById(item.getId())
                    .orElseThrow(() -> new RuntimeException("Stock not found"));

            BigDecimal available = stock.getQuantity();
            BigDecimal requested = itemReq.getQuantity();

            BigDecimal fulfilled = available.compareTo(requested) >= 0
                    ? requested
                    : available;

            BigDecimal pending = requested.subtract(fulfilled);

            BillItem bi = new BillItem();
            bi.setBill(bill);
            bi.setItem(item);
            bi.setQuantity(requested);
            bi.setPrice(itemReq.getPrice());
            bi.setAmount(itemReq.getPrice().multiply(requested));
            bi.setFulfilledQty(fulfilled);
            bi.setPendingQty(pending);

            if (pending.signum() == 0) {
                bi.setFulfilmentStatus("FULL");
            } else if (fulfilled.signum() == 0) {
                bi.setFulfilmentStatus("PENDING");
            } else {
                bi.setFulfilmentStatus("PARTIAL");
            }

            billItemRepo.save(bi);

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

            total = total.add(itemReq.getPrice().multiply(requested));
        }

        bill.setTotalAmount(total);
        bill = billRepo.save(bill);

        if (bill.getPaymentType() == PaymentType.CREDIT) {

            Customer customer = bill.getCustomer();
            BigDecimal oldBalance = customer.getBalance();
            BigDecimal newBalance = oldBalance.add(bill.getTotalAmount());

            customer.setBalance(newBalance);
            customerRepo.save(customer);

            auditService.log(
                    "CUSTOMER",
                    customer.getId(),
                    "CREDIT_OVERRIDE",
                    oldBalance.toString(),
                    newBalance.toString(),
                    user
            );

            CustomerLedger ledger = new CustomerLedger();
            ledger.setCustomer(customer);
            ledger.setBill(bill);
            ledger.setDebit(bill.getTotalAmount());

            ledgerRepo.save(ledger);
        }

        // 📲 WhatsApp Notifications
        if (bill.getCustomer() != null) {

            notificationService.sendWhatsApp(
                    bill.getCustomer().getMobile(),
                    WhatsAppTemplates.billCreated(bill)
            );

            if (bill.getItems().stream()
                    .anyMatch(i -> i.getPendingQty().signum() > 0)) {

                notificationService.sendWhatsApp(
                        bill.getCustomer().getMobile(),
                        WhatsAppTemplates.pendingItems(bill)
                );
            }
        }

        return bill;
    }

    private String generateBillNumber() {
        long next = billRepo.count() + 1;
        return "BILL-" + String.format("%04d", next);
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
