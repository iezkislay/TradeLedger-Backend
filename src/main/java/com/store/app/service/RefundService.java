package com.store.app.service;

import com.store.app.dto.RefundRequest;
import com.store.app.dto.CreateRefundRequest;
import com.store.app.dto.BillSummaryResponse;
import com.store.app.entity.*;
import com.store.app.enums.BillState;
import com.store.app.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import static com.store.app.util.BillGuards.ensureActiveForOps;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class RefundService {

    private final RefundRepository refundRepo;
    private final BillRepository billRepo;
    private final ReturnRepository returnRepo;
    private final ReturnItemRepository returnItemRepo;
    private final BillPriceOverrideRepository billPriceOverrideRepo;
    private final AuthService authService;
    private final AuditService auditService;
    private final ReturnNoteRepository returnNoteRepo;

    public RefundService(
            RefundRepository refundRepo,
            BillRepository billRepo,
            ReturnRepository returnRepo,
            ReturnItemRepository returnItemRepo,
            BillPriceOverrideRepository billPriceOverrideRepo,
            ReturnNoteRepository returnNoteRepo,
            AuthService authService,
            AuditService auditService
    ) {
        this.refundRepo = refundRepo;
        this.billRepo = billRepo;
        this.returnRepo = returnRepo;
        this.returnItemRepo = returnItemRepo;
        this.billPriceOverrideRepo = billPriceOverrideRepo;
        this.returnNoteRepo = returnNoteRepo;
        this.authService = authService;
        this.auditService = auditService;
    }

    /* =====================================================
       💸 REFUND — UNIVERSAL (RETURN-CAPPED, TIMING SAFE)
       ===================================================== */
    @Transactional
    public void processRefund(RefundRequest request, User user) {

        authService.requireOwner(user);

        Bill bill = billRepo.findById(request.getBillId())
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        // 🔒 GUARD
        if (bill.getState() == BillState.ESTIMATE
                || bill.getState() == BillState.CANCELLED) {
            throw new IllegalStateException("Refund not allowed for this bill state");
        }
        ensureActiveForOps(bill);

        BigDecimal finalizedReturnTotal =
                returnNoteRepo.sumFinalizedReturnByBill(bill.getId());

        if (finalizedReturnTotal == null) {
            finalizedReturnTotal = BigDecimal.ZERO;
        }

        BigDecimal refundedSoFar =
                refundRepo.sumRefundedAmountByBillId(bill.getId());

        if (refundedSoFar == null) {
            refundedSoFar = BigDecimal.ZERO;
        }

        BigDecimal remainingRefund =
                finalizedReturnTotal.subtract(refundedSoFar);

        if (remainingRefund.signum() <= 0) {
            throw new IllegalStateException("No refundable amount remaining");
        }

        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalStateException("Refund amount must be positive");
        }

        if (request.getAmount().compareTo(remainingRefund) > 0) {
            throw new IllegalStateException(
                    "Refund amount exceeds remaining refundable value"
            );
        }

        Refund refund = new Refund();
        refund.setBill(bill);
        refund.setCustomer(bill.getCustomer());
        refund.setAmount(request.getAmount());
        refund.setRefundMode(request.getRefundMode());
        refund.setReason(request.getReason());
        refund.setCreatedBy(user);

        refundRepo.save(refund);

        auditService.log(
                "BILL",
                bill.getId(),
                "REFUND",
                null,
                "Refund ₹" + request.getAmount() + " via " + request.getRefundMode(),
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

        ReturnNote returnNote = returnNoteRepo.findById(req.getReturnNoteId())
                .orElseThrow(() -> new RuntimeException("Return note not found"));

        Bill bill = returnNote.getBill();
        // 🔒 GUARD
        if (bill.getState() == BillState.ESTIMATE
                || bill.getState() == BillState.CANCELLED) {
            throw new IllegalStateException("Refund not allowed for this bill state");
        }
        ensureActiveForOps(bill);

        BigDecimal alreadyRefunded =
                refundRepo.sumRefundedAmountByReturnNote(returnNote.getId());

        if (alreadyRefunded == null) {
            alreadyRefunded = BigDecimal.ZERO;
        }

        BigDecimal refundableRemaining =
                returnNote.getNetReturnAmount().subtract(alreadyRefunded);

        if (refundableRemaining.signum() <= 0) {
            throw new IllegalStateException("No refundable amount remaining for this return");
        }

        if (req.getAmount().compareTo(refundableRemaining) > 0) {
            throw new IllegalStateException("Refund exceeds return value");
        }

        Refund refund = new Refund();
        refund.setBill(bill);
        refund.setReturnNote(returnNote); // 🔥 THIS FIXES YOUR ERROR
        refund.setCustomer(bill.getCustomer());
        refund.setAmount(req.getAmount());
        refund.setRefundMode(req.getRefundMode());
        refund.setReason(req.getReason());
        refund.setCreatedBy(user);

        refundRepo.save(refund);

        auditService.log(
                "RETURN",
                returnNote.getId(),
                "REFUND",
                null,
                "Refund ₹" + req.getAmount(),
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
        if (amount == null || amount.signum() <= 0) {
            return;
        }

        Refund refund = new Refund();
        refund.setBill(bill);
        refund.setAmount(amount);
        refund.setReason(reason);
        refund.setCreatedBy(user);

        refundRepo.save(refund);

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
        // 🔒 GUARD
        if (bill.getState() == BillState.ESTIMATE
                || bill.getState() == BillState.CANCELLED) {
            throw new IllegalStateException("Refund not allowed for this bill state");
        }

        Refund refund = new Refund();
        refund.setBill(bill);
        refund.setAmount(refundAmount);
        refund.setReason(reason);
        refund.setCreatedBy(user);

        refundRepo.save(refund);

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
        r.setTotalAmount(bill.getTotalAmount());

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
                refundRepo.getTotalRefundedByBill(billId);

        if (refunded == null) {
            refunded = BigDecimal.ZERO;
        }

        r.setRefundedAmount(refunded);
        r.setRefundableRemaining(effectiveReturn.subtract(refunded));

        return r;
    }
}
