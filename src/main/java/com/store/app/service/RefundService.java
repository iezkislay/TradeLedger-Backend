package com.store.app.service;

import com.store.app.dto.RefundRequest;
import com.store.app.dto.CreateRefundRequest;
import com.store.app.dto.BillSummaryResponse;
import com.store.app.entity.*;
import com.store.app.enums.LedgerType;
import com.store.app.enums.RefundMode;
import com.store.app.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefundService {

    private final RefundRepository refundRepo;
    private final BillRepository billRepo;
    private final ReturnRepository returnRepo;
    private final ReturnItemRepository returnItemRepo;
    private final BillPriceOverrideRepository billPriceOverrideRepo;
    private final CustomerLedgerRepository ledgerRepo;
    private final AuthService authService;
    private final AuditService auditService;
    private final ReturnNoteRepository returnNoteRepo;

    public RefundService(
            RefundRepository refundRepo,
            BillRepository billRepo,
            ReturnRepository returnRepo,
            ReturnItemRepository returnItemRepo,
            BillPriceOverrideRepository billPriceOverrideRepo,
            CustomerLedgerRepository ledgerRepo,
            AuthService authService,
            AuditService auditService,
            ReturnNoteRepository returnNoteRepo
    ) {
        this.refundRepo = refundRepo;
        this.billRepo = billRepo;
        this.returnRepo = returnRepo;
        this.returnItemRepo = returnItemRepo;
        this.billPriceOverrideRepo = billPriceOverrideRepo;
        this.ledgerRepo = ledgerRepo;
        this.authService = authService;
        this.auditService = auditService;
        this.returnNoteRepo = returnNoteRepo;
    }

    /* =====================================================
       💸 LEGACY REFUND — AGAINST BILL (DO NOT TOUCH)
       ===================================================== */
    @Transactional
    public void processRefund(RefundRequest req, User user) {

        authService.requireOwner(user);

        Bill bill = billRepo.findById(req.getBillId())
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        BigDecimal amount = req.getAmount();
        if (amount == null || amount.signum() <= 0) {
            throw new RuntimeException("Refund amount must be positive");
        }

        BigDecimal alreadyRefunded =
                refundRepo.sumRefundedAmountForBill(bill.getId());

        if (alreadyRefunded == null) alreadyRefunded = BigDecimal.ZERO;

        BigDecimal refundable =
                bill.getTotalAmount().subtract(alreadyRefunded);

        if (amount.compareTo(refundable) > 0) {
            throw new RuntimeException("Refund exceeds refundable amount");
        }

        Refund refund = new Refund();
        refund.setBill(bill);
        refund.setCustomer(bill.getCustomer());
        refund.setAmount(amount);
        refund.setRefundMode(req.getRefundMode());
        refund.setReason(req.getReason());
        refund.setCreatedBy(user);

        refundRepo.save(refund);

        if (req.getRefundMode() == RefundMode.CREDIT) {

            Customer customer = bill.getCustomer();
            if (customer == null) {
                throw new RuntimeException("CREDIT refund requires customer");
            }

            CustomerLedger ledger = new CustomerLedger();
            ledger.setCustomer(customer);
            ledger.setBill(bill);
            ledger.setEntryType(LedgerType.CREDIT);
            ledger.setAmount(amount);

            ledgerRepo.save(ledger);
        }

        auditService.log(
                "BILL",
                bill.getId(),
                "REFUND",
                null,
                "Refund ₹" + amount + " via " + req.getRefundMode(),
                user
        );
    }

    /* =====================================================
       💸 REFUND — AGAINST RETURN (STEP B)
       ===================================================== */
    @Transactional
    public void refundAgainstReturn(CreateRefundRequest req, User user) {

        authService.requireBillingOrOwner(user);

        if (req.getAmount() == null || req.getAmount().signum() <= 0) {
            throw new RuntimeException("Refund amount must be positive");
        }

        Return ret = returnRepo.findById(req.getReturnId())
                .orElseThrow(() -> new RuntimeException("Return not found"));

        Customer customer = ret.getCustomer();
        if (customer == null) {
            throw new RuntimeException("Refund requires customer");
        }

        Refund refund = new Refund();
        refund.setReturnEntity(ret);
        refund.setBill(ret.getBill());
        refund.setCustomer(customer);
        refund.setAmount(req.getAmount());
        refund.setRefundMode(req.getRefundMode());
        refund.setReason(req.getReason());
        refund.setCreatedBy(user);

        refundRepo.save(refund);

        CustomerLedger ledger = new CustomerLedger();
        ledger.setCustomer(customer);
        ledger.setBill(ret.getBill());
        ledger.setEntryType(LedgerType.CREDIT);
        ledger.setAmount(req.getAmount());

        ledgerRepo.save(ledger);

        auditService.log(
                "REFUND",
                refund.getId(),
                "RETURN_REFUND",
                null,
                "Refund ₹" + req.getAmount() + " against Return " + ret.getId(),
                user
        );
    }

    /* =====================================================
       🆕 TRUSTED INTERNAL REFUND (NO VALIDATION)
       ===================================================== */
    @Transactional
    void createRefundUnchecked(
            Bill bill,
            BigDecimal amount,
            String reason,
            User user
    ) {
        if (amount == null || amount.signum() <= 0) return;

        Refund refund = new Refund();
        refund.setBill(bill);
        refund.setAmount(amount);
        refund.setReason(reason);
        refund.setCreatedBy(user);
        refundRepo.save(refund);

        if (bill.getCustomer() != null) {
            CustomerLedger ledger = new CustomerLedger();
            ledger.setCustomer(bill.getCustomer());
            ledger.setBill(bill);
            ledger.setEntryType(LedgerType.CREDIT);
            ledger.setAmount(amount);
            ledgerRepo.save(ledger);
        }

        auditService.log(
                "REFUND",
                refund.getId(),
                "RETURN_FINALIZE",
                null,
                amount.toString(),
                user
        );
    }

    /* =====================================================
       🆕 REFUND FROM FINALIZED RETURN NOTE
       ===================================================== */
    @Transactional
    public void refundFromReturnNote(
            Bill bill,
            BigDecimal refundAmount,
            User user
    ) {
        if (refundAmount == null || refundAmount.signum() <= 0) {
            return;
        }

        createRefund(
                bill.getId(),
                refundAmount,
                "Refund against sales return",
                user
        );
    }

    /* =====================================================
       ✅ CORE REFUND CREATION — FINALIZED RETURNS ONLY
       ===================================================== */
    @Transactional
    public void createRefund(
            UUID billId,
            BigDecimal refundAmount,
            String reason,
            User user
    ) {
        authService.requireBillingOrOwner(user);

        if (refundAmount == null || refundAmount.signum() <= 0) {
            return;
        }

        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        Refund refund = new Refund();
        refund.setBill(bill);
        refund.setAmount(refundAmount);
        refund.setReason(reason);
        refund.setCreatedBy(user);

        refundRepo.save(refund);

        if (bill.getCustomer() != null) {
            CustomerLedger ledger = new CustomerLedger();
            ledger.setCustomer(bill.getCustomer());
            ledger.setBill(bill);
            ledger.setEntryType(LedgerType.CREDIT);
            ledger.setAmount(refundAmount);
            ledgerRepo.save(ledger);
        }

        auditService.log(
                "REFUND",
                refund.getId(),
                "CREATE",
                null,
                refundAmount.toString(),
                user
        );
    }


    /* =====================================================
       🧾 BILL SUMMARY — UNCHANGED
       ===================================================== */
    @Transactional(readOnly = true)
    public BillSummaryResponse getBillSummary(UUID billId, User user) {

        authService.requireBillingOrOwner(user);

        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        BillSummaryResponse r = new BillSummaryResponse();

        r.setBillId(bill.getId());
        r.setBillCode(bill.getBillCode());
        r.setBillAmount(bill.getTotalAmount());

        BigDecimal overriddenAmount =
                billPriceOverrideRepo.findByBill_Id(billId)
                        .map(BillPriceOverride::getOverriddenAmount)
                        .orElse(bill.getTotalAmount());

        r.setOverriddenAmount(overriddenAmount);

        Object[] split = returnItemRepo.returnValueSplit(billId);
        BigDecimal delivered = (BigDecimal) split[0];
        BigDecimal pending = (BigDecimal) split[1];

        BigDecimal grossReturn = delivered.add(pending);

        r.setDeliveredReturnValue(delivered);
        r.setPendingReturnValue(pending);
        r.setReturnedGrossValue(grossReturn);

        BigDecimal effectiveReturn = BigDecimal.ZERO;
        if (bill.getTotalAmount().signum() > 0) {
            effectiveReturn =
                    grossReturn
                            .multiply(overriddenAmount)
                            .divide(bill.getTotalAmount(), 2, RoundingMode.HALF_UP);
        }

        r.setReturnedEffectiveValue(effectiveReturn);

        BigDecimal refunded =
                refundRepo.sumRefundedAmountByBill(billId);
        if (refunded == null) refunded = BigDecimal.ZERO;

        r.setRefundedAmount(refunded);
        r.setRefundableRemaining(
                bill.getTotalAmount().subtract(refunded)
        );

        return r;
    }
}
