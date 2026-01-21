package com.store.app.service;

import com.store.app.dto.ReturnItemRequest;
import com.store.app.entity.*;
import com.store.app.enums.PaymentType;
import com.store.app.enums.LedgerType;
import com.store.app.enums.ReferenceType;
import com.store.app.enums.ReturnSource;
import com.store.app.enums.StockTxnType;
import com.store.app.enums.RefundMode;
import com.store.app.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import static com.store.app.util.BillGuards.ensureActiveForOps;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReturnService {

    private final BillItemRepository billItemRepo;
    private final ReturnItemRepository returnItemRepo;
    private final ReturnNoteRepository returnNoteRepo;
    private final CustomerLedgerRepository ledgerRepo;
    private final StockRepository stockRepo;
    private final StockTransactionRepository stockTxnRepo;
    private final RefundRepository refundRepo;
    private final RefundService refundService;
    private final AuditService auditService;

    public ReturnService(
            BillItemRepository billItemRepo,
            ReturnItemRepository returnItemRepo,
            ReturnNoteRepository returnNoteRepo,
            CustomerLedgerRepository ledgerRepo,
            StockRepository stockRepo,
            StockTransactionRepository stockTxnRepo,
            RefundRepository refundRepo,
            RefundService refundService,
            AuditService auditService
    ) {
        this.billItemRepo = billItemRepo;
        this.returnItemRepo = returnItemRepo;
        this.returnNoteRepo = returnNoteRepo;
        this.ledgerRepo = ledgerRepo;
        this.stockRepo = stockRepo;
        this.stockTxnRepo = stockTxnRepo;
        this.refundRepo = refundRepo;
        this.refundService = refundService;
        this.auditService = auditService;
    }

    /* =====================================================
   STEP 1 — CREATE RETURN NOTE (USER-INTENT SAFE)
   ===================================================== */
    @Transactional
    public ReturnNote createReturnNote(
            List<ReturnItemRequest> items,
            User user
    ) {

        if (items == null || items.isEmpty()) {
            throw new RuntimeException("No return items provided");
        }

        Bill bill = null;
        BigDecimal returnedGross = BigDecimal.ZERO;

        ReturnNote note = new ReturnNote();
        note.setCreatedBy(user);

        for (ReturnItemRequest req : items) {

            if (req.getReturnSource() == null) {
                throw new RuntimeException("Return source is required");
            }

            BillItem billItem = billItemRepo.findById(req.getBillItemId())
                    .orElseThrow(() -> new RuntimeException("Bill item not found"));

            if (bill == null) {
                bill = billItem.getBill();
                ensureActiveForOps(bill);
                note.setBill(bill);
                returnNoteRepo.save(note); // needed for FK usage
            } else if (!bill.getId().equals(billItem.getBill().getId())) {
                throw new RuntimeException("All return items must belong to same bill");
            }

            BigDecimal requestedQty = req.getReturnedQuantity();
            if (requestedQty == null || requestedQty.signum() <= 0) {
                throw new RuntimeException("Returned quantity must be positive");
            }

            BigDecimal delivered = BigDecimal.ZERO;
            BigDecimal pending = BigDecimal.ZERO;

            if (req.getReturnSource() == ReturnSource.DELIVERED) {

                BigDecimal alreadyReturned =
                        returnItemRepo.getTotalReturnedDeliveredQtyForBillItem(billItem.getId());

                BigDecimal maxAllowed =
                        billItem.getFulfilledQty().subtract(alreadyReturned);

                if (requestedQty.compareTo(maxAllowed) > 0) {
                    throw new RuntimeException("Delivered return exceeds fulfilled qty");
                }

                delivered = requestedQty;

                Stock stock = stockRepo.findById(billItem.getItem().getId())
                        .orElseThrow(() -> new RuntimeException("Stock not found"));

                stock.setQuantity(stock.getQuantity().add(delivered));
                stockRepo.save(stock);

                StockTransaction txn = new StockTransaction();
                txn.setItem(billItem.getItem());
                txn.setTransactionType(StockTxnType.IN);
                txn.setQuantity(delivered);
                txn.setReferenceType(ReferenceType.RETURN);
                txn.setReferenceId(note.getId());
                stockTxnRepo.save(txn);

            } else {

                BigDecimal alreadyReturned =
                        returnItemRepo.getTotalReturnedPendingQtyForBillItem(billItem.getId());

                BigDecimal maxAllowed =
                        billItem.getPendingQty().subtract(alreadyReturned);

                if (requestedQty.compareTo(maxAllowed) > 0) {
                    throw new RuntimeException("Pending return exceeds pending qty");
                }

                pending = requestedQty;
            }

            BigDecimal lineAmount =
                    requestedQty.multiply(billItem.getPrice());

            returnedGross = returnedGross.add(lineAmount);

            ReturnItem ri = new ReturnItem();
            ri.setReturnNote(note);
            ri.setBill(bill);
            ri.setBillItem(billItem);
            ri.setItem(billItem.getItem());
            ri.setReturnedDeliveredQty(delivered);
            ri.setReturnedPendingQty(pending);
            ri.setPrice(billItem.getPrice());

            returnItemRepo.save(ri);
        }

    /* ===============================
       BASE CALCULATION
       =============================== */

        BigDecimal billSubtotal = bill.getSubtotal();
        BigDecimal billDiscount = bill.getDiscountAmount();

        BigDecimal returnRatio =
                returnedGross.divide(billSubtotal, 6, RoundingMode.HALF_UP);

        BigDecimal clawedDiscount =
                billDiscount.multiply(returnRatio)
                        .setScale(2, RoundingMode.HALF_UP);

        BigDecimal netReturn =
                returnedGross.subtract(clawedDiscount);

    /* ===============================
       🔥 RESIDUAL ADJUSTMENT (HERE)
       =============================== */

        BigDecimal alreadyReturned =
                returnNoteRepo.sumNetReturnsByBill(bill.getId());

        BigDecimal billEffectiveTotal =
                bill.getSubtotal().subtract(bill.getDiscountAmount());

        BigDecimal remainingCapacity =
                billEffectiveTotal.subtract(alreadyReturned);

        if (remainingCapacity.signum() < 0) {
            throw new IllegalStateException("Returns already exceed bill total");
        }

        if (netReturn.compareTo(remainingCapacity) > 0) {

            BigDecimal delta =
                    netReturn.subtract(remainingCapacity);

            if (delta.abs().compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalStateException(
                        "Residual delta exceeds tolerance (₹1.00)"
                );
            }

            // Adjust
            netReturn = remainingCapacity;
            clawedDiscount = clawedDiscount.add(delta);

            note.setResidualAdjusted(true);
            note.setAdjustmentNote(
                    "Rounded by ₹" + delta.setScale(2) + " to match Bill total"
            );

            auditService.log(
                    "RETURN_NOTE",
                    note.getId(),
                    "ROUNDING_RESIDUAL",
                    null,
                    note.getAdjustmentNote(),
                    user
            );
        }

    /* ===============================
       SAVE FINAL VALUES
       =============================== */

        note.setReturnedGrossAmount(returnedGross);
        note.setClawedDiscountAmount(clawedDiscount);
        note.setNetReturnAmount(netReturn);

        returnNoteRepo.save(note);
        return note;
    }

    /* =====================================================
   STEP 2 — FINALIZE RETURN (EXECUTION ONLY)
   ===================================================== */
    @Transactional
    public void finalizeReturn(UUID returnNoteId, User user) {

        ReturnNote note = returnNoteRepo.findById(returnNoteId)
                .orElseThrow(() -> new RuntimeException("Return note not found"));

        if (note.isFinalized()) {
            return; // idempotent
        }

        Bill bill = note.getBill();
        ensureActiveForOps(bill);

        BigDecimal effectiveReturn = note.getNetReturnAmount();

    /* =====================================================
       1️⃣ CREDIT LEDGER (DUE-ONLY ADJUSTMENT)
       ===================================================== */

        if (bill.getPaymentType() == PaymentType.CREDIT) {

            BigDecimal currentDue =
                    ledgerRepo.getCustomerBalance(bill.getId());

            if (currentDue == null || currentDue.signum() <= 0) {
                // Nothing owed → ledger must NOT be touched
                // Excess will be handled via refund flow
            } else {

                BigDecimal ledgerAdjustment =
                        effectiveReturn.min(currentDue);

                if (ledgerAdjustment.signum() > 0) {

                    CustomerLedger entry = new CustomerLedger();
                    entry.setBill(bill);
                    entry.setCustomer(bill.getCustomer());
                    entry.setEntryType(LedgerType.RETURN_CREDIT);
                    entry.setReferenceType(ReferenceType.RETURN);
                    entry.setAmount(ledgerAdjustment);

                    ledgerRepo.save(entry);
                }
            }
        }

    /* =====================================================
       2️⃣ AUTO REFUND (OPTION B — ONLY IF NONE EXISTS)
       ===================================================== */

        BigDecimal refundedSoFar =
                refundRepo.sumRefundedAmountByReturnNoteId(note.getId());

        if (refundedSoFar == null) {
            refundedSoFar = BigDecimal.ZERO;
        }

        BigDecimal refundableRemaining =
                effectiveReturn.subtract(refundedSoFar);

        if (refundedSoFar.signum() == 0 && refundableRemaining.signum() > 0) {

            Refund refund = new Refund();
            refund.setBill(bill);
            refund.setReturnNote(note);
            refund.setAmount(refundableRemaining);
            refund.setRefundMode(
                    bill.getPaymentType() == PaymentType.UPI
                            ? RefundMode.UPI
                            : RefundMode.CASH
            );
            refund.setReason("Auto refund on return finalization");

            refundRepo.save(refund);
        }

    /* =====================================================
       3️⃣ FINALIZE NOTE
       ===================================================== */

        note.setFinalized(true);
        note.setFinalizedAt(LocalDateTime.now());

        returnNoteRepo.save(note);
    }
}
