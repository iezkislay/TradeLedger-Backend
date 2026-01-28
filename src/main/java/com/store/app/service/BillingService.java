package com.store.app.service;

import org.springframework.transaction.annotation.Transactional;
import com.store.app.dto.*;
import com.store.app.entity.*;
import com.store.app.enums.*;
import com.store.app.repository.*;
import com.store.app.util.WhatsAppTemplates;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static com.store.app.util.BillGuards.ensureNotClosed;

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

    private final BillPriceOverrideRepository billPriceOverrideRepo;
    private final ReturnItemRepository returnItemRepo;
    private final ReturnNoteRepository returnNoteRepo;
    private final RefundRepository refundRepo;

    @Getter
    private final NotificationService notificationService;
    private LocalDateTime now;

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
            BillPriceOverrideRepository billPriceOverrideRepo,
            ReturnItemRepository returnItemRepo,
            ReturnNoteRepository returnNoteRepo,
            RefundRepository refundRepo,
            NotificationService notificationService
    )
    {
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
        this.billPriceOverrideRepo = billPriceOverrideRepo;
        this.returnItemRepo = returnItemRepo;
        this.returnNoteRepo = returnNoteRepo;
        this.refundRepo = refundRepo;
        this.notificationService = notificationService;
    }


    /* =====================================================
       CREATE BILL — UNCHANGED
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

        if (req.getPaymentType() == PaymentType.CREDIT && customer == null) {

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

        if (req.getPaymentType() != PaymentType.CREDIT
                && customer == null
                && req.getCustomerName() != null
                && !req.getCustomerName().isBlank()
                && req.getCustomerMobile() != null
                && !req.getCustomerMobile().isBlank()) {

            customer = customerService.createCustomer(
                    req.getCustomerName().trim(),
                    req.getCustomerMobile().trim(),
                    req.getCustomerAddress()
            );
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

            BigDecimal fulfilled = stock.getQuantity().min(itemReq.getQuantity());
            BigDecimal pending = itemReq.getQuantity().subtract(fulfilled);

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

        if (bill.getPaymentType() == PaymentType.CREDIT) {

            // 🔴 1️⃣ Bill raised → full amount
            CustomerLedger debit = new CustomerLedger();
            debit.setCustomer(customer);
            debit.setBill(bill);
            debit.setEntryType(LedgerType.DEBIT);
            debit.setReferenceType(ReferenceType.BILL);
            debit.setAmount(finalAmount);
            ledgerRepo.save(debit);

            // 🟢 2️⃣ Money received at billing time
            if (amountPaid.signum() > 0) {
                CustomerLedger credit = new CustomerLedger();
                credit.setEntryType(LedgerType.CREDIT);
                credit.setReferenceType(ReferenceType.PAYMENT);
                credit.setAmount(amountPaid);

                credit.setCustomer(customer);
                credit.setBill(bill);

                credit.setAmount(amountPaid);
                ledgerRepo.save(credit);
            }
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
       🆕 OVERRIDE BILL PRICE (EXTENSION — SAFE)
       ===================================================== */
    @Transactional
    public void overrideBillPrice(
            UUID billId,
            BillOverrideRequest request,
            User user
    ) {

        authService.requireBillingOrOwner(user);

        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        ensureNotClosed(bill);

        BigDecimal originalAmount = bill.getTotalAmount();
        BigDecimal overridden = request.getOverriddenAmount();

        if (overridden == null ||
                overridden.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid overridden amount");
        }

        if (overridden.compareTo(originalAmount) > 0) {
            throw new RuntimeException("Override cannot exceed bill total");
        }

        if (billPriceOverrideRepo.findByBill_Id(billId).isPresent()) {
            throw new RuntimeException("Bill already overridden");
        }

        BillPriceOverride o = new BillPriceOverride();
        o.setBill(bill);
        o.setOriginalAmount(originalAmount);
        o.setOverriddenAmount(overridden);
        o.setReason(request.getReason());
        o.setOverriddenBy(user);
        o.setOverriddenAt(LocalDateTime.now());

        billPriceOverrideRepo.save(o);

        BigDecimal discount = originalAmount.subtract(overridden);

        if (discount.signum() > 0 && bill.getCustomer() != null) {

            CustomerLedger ledger = new CustomerLedger();
            ledger.setCustomer(bill.getCustomer());
            ledger.setBill(bill);
            ledger.setEntryType(LedgerType.CREDIT);
            ledger.setReferenceType(ReferenceType.PRICE_OVERRIDE);
            ledger.setAmount(discount);
            ledger.setCreatedAt(LocalDateTime.now());

            ledgerRepo.save(ledger);
        }

        auditService.log(
                "BILL",
                billId,
                "PRICE_OVERRIDE",
                originalAmount.toString(),
                overridden.toString(),
                user
        );
    }
    /* =====================================================
   READ — BILL DETAILS (LEDGER TRUTH)
   ===================================================== */
    public BillResponse getBillById(UUID billId) {

        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        // ==============================
        // 🔹 DUE — LEDGER SOURCE OF TRUTH
        // ==============================
        BigDecimal dueFromLedger = ledgerRepo.getDueForBill(billId);
        if (dueFromLedger == null) {
            dueFromLedger = BigDecimal.ZERO;
        }

        // ==============================
        // 🔹 RETURNED VALUE (ERP TRUTH)
        // ==============================
        BigDecimal returnedValue = returnItemRepo.sumReturnValueByBill(billId);
        if (returnedValue == null) {
            returnedValue = BigDecimal.ZERO;
        }

        // ==============================
        // 🔹 EFFECTIVE TOTAL (CAP)
        // ==============================
        BigDecimal effectiveTotal =
                bill.getTotalAmount().max(BigDecimal.ZERO);

        // ==============================
        // 🔹 CASH PAID ONLY (CREDIT ENTRIES)
        // ==============================
        BigDecimal amountPaid =
                ledgerRepo.getActualPaidAmount(billId);

        if (amountPaid == null) {
            amountPaid = BigDecimal.ZERO;
        }

        // 🔒 NEVER EXPOSE NEGATIVE DUE
        BigDecimal safeDue = dueFromLedger.max(BigDecimal.ZERO);

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

        res.setReturnedAmount(returnedValue);
        res.setEffectiveTotal(effectiveTotal);

        // ✅ FINAL CORRECT VALUES
        res.setAmountPaid(amountPaid);
        res.setDueAmount(safeDue);
        res.setState(bill.getState().name());

        return res;
    }

    /* =====================================================
   READ — BILL ITEMS (DOMAIN ONLY)
   ===================================================== */
    public List<BillItem> getBillItems(UUID billId) {

        billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        return billItemRepo.findByBillId(billId);
    }


    /* =====================================================
       READ — LIST / SEARCH BILLS (UNCHANGED)
       ===================================================== */
    public List<BillListResponse> listBills(String search) {

        return billRepo.searchBills(
                        (search == null || search.isBlank())
                                ? null
                                : search.trim()
                )
                .stream()
                .map(bill -> {

                    BigDecimal due = ledgerRepo.getDueForBill(bill.getId());
                    BigDecimal paid = bill.getTotalAmount().subtract(due);

                    BillListResponse r = new BillListResponse();
                    r.setBillId(bill.getId());
                    r.setBillNumber(bill.getBillNumber());
                    r.setBillCode(bill.getBillCode());
                    r.setBillDate(bill.getCreatedAt());
                    r.setPaymentType(bill.getPaymentType().name());

                    if (bill.getCustomer() != null) {
                        r.setCustomerName(bill.getCustomer().getName());
                        r.setCustomerMobile(bill.getCustomer().getMobile());
                    }

                    r.setTotalAmount(bill.getTotalAmount());
                    r.setPaidAmount(paid);
                    r.setDueAmount(due);
                    r.setState(bill.getState().name());

                    return r;
                })
                .toList();
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
                due,
                bill.getState().name()
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
        if (bill.getState() == BillState.ESTIMATE
                || bill.getState() == BillState.CANCELLED) {
            throw new IllegalStateException("Operation not allowed for this bill state");
        }
        ensureNotClosed(bill);

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
            credit.setReferenceType(ReferenceType.PAYMENT);
            credit.setAmount(paid);
            ledgerRepo.save(credit);
        }

        if (adjustment.signum() > 0) {
            CustomerLedger waive = new CustomerLedger();
            waive.setCustomer(bill.getCustomer());
            waive.setBill(bill);
            waive.setEntryType(LedgerType.ADJUSTMENT);
            waive.setReferenceType(ReferenceType.ADJUSTMENT);
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
    /* =====================================================
   CLOSE BILL (HARD FINALIZATION)
   ===================================================== */
    @Transactional
    public void closeBill(UUID billId, User user) {

        authService.requireOwner(user);

        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        if (bill.getState() == BillState.CLOSED) {
            throw new IllegalStateException("Bill already closed");
        }

        if (bill.getState() == BillState.ESTIMATE) {
            throw new IllegalStateException("Estimate bill cannot be closed");
        }

        if (bill.getState() == BillState.CANCELLED) {
            throw new IllegalStateException("Bill is cancelled");
        }

        // 1️⃣ Ledger must be settled
        BigDecimal debit =
                ledgerRepo.sumByBillAndType(billId, LedgerType.DEBIT);

        BigDecimal credit =
                ledgerRepo.sumByBillAndType(billId, LedgerType.CREDIT);

        BigDecimal returnCredit =
                ledgerRepo.sumByBillAndType(billId, LedgerType.RETURN_CREDIT);

        BigDecimal adjustment =
                ledgerRepo.sumByBillAndType(billId, LedgerType.ADJUSTMENT);

        debit = debit == null ? BigDecimal.ZERO : debit;
        credit = credit == null ? BigDecimal.ZERO : credit;
        returnCredit = returnCredit == null ? BigDecimal.ZERO : returnCredit;
        adjustment = adjustment == null ? BigDecimal.ZERO : adjustment;

        BigDecimal netBalance =
                debit.subtract(credit).subtract(returnCredit).subtract(adjustment);

        if (netBalance.signum() != 0) {
            throw new IllegalStateException("Bill cannot be closed: ledger not settled");
        }

        // 2️⃣ No pending fulfilments
        boolean hasPendingFulfilments =
                billItemRepo.findPendingFulfilments().stream()
                        .anyMatch(bi -> bi.getBill().getId().equals(billId));

        if (hasPendingFulfilments) {
            throw new IllegalStateException("Bill cannot be closed: pending fulfilments exist");
        }

        // 3️⃣ All returns must be finalized
        boolean hasUnfinalizedReturns =
                returnNoteRepo.findByBill_Id(billId).stream()
                        .anyMatch(rn -> !rn.isFinalized());

        if (hasUnfinalizedReturns) {
            throw new IllegalStateException("Bill cannot be closed: unfinalized returns exist");
        }

        bill.setState(BillState.CLOSED);
        bill.setClosedAt(LocalDateTime.now());
        billRepo.save(bill);

        auditService.log(
                "BILL",
                billId,
                "CLOSE",
                null,
                "Bill closed",
                user
        );
    }
    /* =====================================================
   BILL AUDIT (READ-ONLY, FULL CONSISTENCY VIEW)
   ===================================================== */
    @Transactional(readOnly = true)
    public BillAuditResponse getBillAudit(UUID billId, User user) {

        authService.requireBillingOrOwner(user);

        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        // =========================
        /* LEDGER (SOURCE OF TRUTH) */
// =========================
        BigDecimal debit =
                ledgerRepo.sumByBillAndType(billId, LedgerType.DEBIT);

        BigDecimal credit =
                ledgerRepo.sumByBillAndType(billId, LedgerType.CREDIT);

        BigDecimal returnCredit =
                ledgerRepo.sumByBillAndType(billId, LedgerType.RETURN_CREDIT);

        BigDecimal adjustment =
                ledgerRepo.sumByBillAndType(billId, LedgerType.ADJUSTMENT);

        debit = debit == null ? BigDecimal.ZERO : debit;
        credit = credit == null ? BigDecimal.ZERO : credit;
        returnCredit = returnCredit == null ? BigDecimal.ZERO : returnCredit;
        adjustment = adjustment == null ? BigDecimal.ZERO : adjustment;

        BigDecimal netBalance =
                debit.subtract(credit)
                        .subtract(returnCredit)
                        .subtract(adjustment);

        // Goods
        BigDecimal totalOrdered = billItemRepo.findByBillId(billId).stream()
                .map(BillItem::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFulfilled = billItemRepo.findByBillId(billId).stream()
                .map(BillItem::getFulfilledQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReturned = billItemRepo.findByBillId(billId).stream()
                .map(bi -> returnItemRepo.getTotalReturnedQtyForBillItem(bi.getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Returns
        Object agg = returnNoteRepo.getReturnAggregates(billId);
        Object[] returnAgg = (Object[]) agg;

        BigDecimal returnedGross = (BigDecimal) returnAgg[0];
        BigDecimal returnedEffective = (BigDecimal) returnAgg[1];

        // Refunds
        BigDecimal refundedTotal =
                refundRepo.sumRefundedAmountByBill(billId);

        return new BillAuditResponse(
                bill.getId(),
                bill.getBillCode(),
                bill.getState().name(),

                debit,
                credit,
                returnCredit,
                adjustment,
                netBalance,

                totalOrdered,
                totalFulfilled,
                totalReturned,

                returnedGross,
                returnedEffective,

                refundedTotal
        );
    }

    // =====================================================
    // 📜 Create ESTIMATE Bill
    // =====================================================

    @Transactional
    public Bill createEstimate(CreateBillRequest req, User user) {

        authService.requireBillingOrOwner(user);
        validationService.validateBillItems(req.getItems());

    /* =====================================================
       1️⃣ CALCULATE TOTALS (NO PAYMENT / NO LEDGER)
       ===================================================== */

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

    /* =====================================================
       2️⃣ RESOLVE CUSTOMER (SAME AS createBill, NO PAYMENT)
       ===================================================== */

        Customer customer = null;

        // Case 1: existing customer selected
        if (req.getCustomerId() != null) {
            customer = customerRepo.findById(req.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
        }

        // Case 2: customer details entered manually
        if (customer == null
                && req.getCustomerName() != null
                && !req.getCustomerName().isBlank()
                && req.getCustomerMobile() != null
                && !req.getCustomerMobile().isBlank()) {

            customer = customerService.createCustomer(
                    req.getCustomerName().trim(),
                    req.getCustomerMobile().trim(),
                    req.getCustomerAddress()
            );
        }

    /* =====================================================
       3️⃣ CREATE ESTIMATE BILL (NUMBERING FIX APPLIED)
       ===================================================== */

        Bill bill = new Bill();
        bill.setBillNumber(generateBillNumber());
        bill.setBillCode(generateBillCode());
//        bill.setBillNumber(generateEstimateSequence()); // ✅ EST-0001
//        bill.setBillCode(generateEstimateCode());       // ✅ EST-YYYYMMDDXX
        bill.setPaymentType(PaymentType.ESTIMATE);
        bill.setState(BillState.ESTIMATE);

        bill.setCustomer(customer);
        bill.setCreatedBy(user);
        bill.setSubtotal(subtotal);
        bill.setDiscountAmount(discount);
        bill.setTotalAmount(finalAmount);

        billRepo.save(bill);

    /* =====================================================
       4️⃣ ATTACH ITEMS (NO STOCK MOVEMENT)
       ===================================================== */

        for (BillItemRequest itemReq : req.getItems()) {

            Item item = itemRepo.findById(itemReq.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            validationService.validateQuantity(item.getBaseUnit(), itemReq.getQuantity());
            validationService.validatePrice(itemReq.getPrice(), item.getCostPrice());

            BillItem bi = new BillItem();
            bi.setBill(bill);
            bi.setItem(item);
            bi.setQuantity(itemReq.getQuantity());
            bi.setPrice(itemReq.getPrice());
            bi.setAmount(itemReq.getPrice().multiply(itemReq.getQuantity()));

            bi.setFulfilledQty(BigDecimal.ZERO);
            bi.setPendingQty(itemReq.getQuantity());
            bi.setFulfilmentStatus("ESTIMATE");

            bill.getItems().add(bi);
        }

    /* =====================================================
       5️⃣ AUDIT
       ===================================================== */

        auditService.log(
                "BILL",
                bill.getId(),
                "CREATE_ESTIMATE",
                null,
                finalAmount.toString(),
                user
        );

    /* =====================================================
       6️⃣ WHATSAPP (DEFENSIVE — NO CRASH)
       ===================================================== */

        if (bill.getCustomer() != null && bill.getCustomer().getMobile() != null) {
            notificationService.sendWhatsApp(
                    bill.getCustomer().getMobile(),
                    WhatsAppTemplates.estimate(bill)
            );
        }

        return bill;
    }

    /* =====================================================
   🔥 ACTIVATE ESTIMATE → ACTIVE
   ===================================================== */
    @Transactional
    public Bill activateEstimate(
            UUID billId,
            PaymentType paymentType,
            BigDecimal amountPaid,
            User user
    ) {

        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        if (bill.getState() != BillState.ESTIMATE) {
            throw new RuntimeException("Only ESTIMATE bills can be activated");
        }

        // 🔁 Re-run validations
        validationService.validateBillItems(
                bill.getItems().stream()
                        .map(bi -> {
                            BillItemRequest r = new BillItemRequest();
                            r.setItemId(bi.getItem().getId());
                            r.setQuantity(bi.getQuantity());
                            r.setPrice(bi.getPrice());
                            return r;
                        })
                        .toList()
        );

        // 🔢 Assign official numbers NOW
        bill.setPaymentType(paymentType);
        bill.setState(BillState.ACTIVE);
        bill.setActivatedAt(LocalDateTime.now());

        BigDecimal finalAmount = bill.getTotalAmount();

        if (amountPaid.signum() < 0 || amountPaid.compareTo(finalAmount) > 0) {
            throw new RuntimeException("Invalid paid amount");
        }

        BigDecimal dueAmount = finalAmount.subtract(amountPaid);

        // 📦 Fulfil stock NOW
        fulfilStockAndCreateTransactions(bill);

        // 💰 Ledger NOW
        if (paymentType == PaymentType.CREDIT) {

            CustomerLedger debit = new CustomerLedger();
            debit.setCustomer(bill.getCustomer());
            debit.setBill(bill);
            debit.setEntryType(LedgerType.DEBIT);
            debit.setReferenceType(ReferenceType.BILL);
            debit.setAmount(finalAmount);
            ledgerRepo.save(debit);

            if (amountPaid.signum() > 0) {
                CustomerLedger credit = new CustomerLedger();
                credit.setCustomer(bill.getCustomer());
                credit.setBill(bill);
                credit.setEntryType(LedgerType.CREDIT);
                credit.setReferenceType(ReferenceType.PAYMENT);
                credit.setAmount(amountPaid);
                ledgerRepo.save(credit);
            }
        }

        auditService.log(
                "BILL",
                bill.getId(),
                "ACTIVATE_BILL",
                "ESTIMATE",
                "ACTIVE",
                user
        );

        notificationService.sendWhatsApp(
                bill.getCustomer().getMobile(),
                WhatsAppTemplates.billCreated(bill)
        );

        return bill;
    }

    private void fulfilStockAndCreateTransactions(Bill bill) {
        for (BillItem bi : bill.getItems()) {

            Stock stock = stockRepo.findById(bi.getItem().getId())
                    .orElseThrow(() -> new RuntimeException("Stock not found"));

            BigDecimal fulfilled = stock.getQuantity().min(bi.getQuantity());
            BigDecimal pending = bi.getQuantity().subtract(fulfilled);

            bi.setFulfilledQty(fulfilled);
            bi.setPendingQty(pending);

            bi.setFulfilmentStatus(
                    pending.signum() == 0 ? "FULL"
                            : fulfilled.signum() == 0 ? "PENDING"
                            : "PARTIAL"
            );

            if (fulfilled.signum() > 0) {
                stock.setQuantity(stock.getQuantity().subtract(fulfilled));
                stockRepo.save(stock);

                StockTransaction txn = new StockTransaction();
                txn.setItem(bi.getItem());
                txn.setTransactionType(StockTxnType.OUT);
                txn.setQuantity(fulfilled);
                txn.setReferenceType(ReferenceType.BILL);
                txn.setReferenceId(bill.getId());
                stockTxnRepo.save(txn);
            }
        }
    }

    @Transactional
    public void cancelEstimate(UUID billId, User user) {

        authService.requireBillingOrOwner(user);

        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        if (bill.getState() != BillState.ESTIMATE) {
            throw new IllegalStateException("Only ESTIMATE bills can be cancelled");
        }

        bill.setState(BillState.CANCELLED);
        bill.setCancelledAt(LocalDateTime.now());

        billRepo.save(bill);

        auditService.log(
                "BILL",
                billId,
                "CANCEL_ESTIMATE",
                "ESTIMATE",
                "CANCELLED",
                user
        );
    }

}
