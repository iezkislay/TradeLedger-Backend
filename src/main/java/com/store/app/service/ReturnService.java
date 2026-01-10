package com.store.app.service;

import com.store.app.dto.ReturnItemRequest;
import com.store.app.entity.*;
import com.store.app.enums.LedgerType;
import com.store.app.enums.ReferenceType;
import com.store.app.enums.ReturnSource;
import com.store.app.enums.StockTxnType;
import com.store.app.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
    private final RefundService refundService;

    public ReturnService(
            BillItemRepository billItemRepo,
            ReturnItemRepository returnItemRepo,
            ReturnNoteRepository returnNoteRepo,
            CustomerLedgerRepository ledgerRepo,
            StockRepository stockRepo,
            StockTransactionRepository stockTxnRepo,
            RefundService refundService
    ) {
        this.billItemRepo = billItemRepo;
        this.returnItemRepo = returnItemRepo;
        this.returnNoteRepo = returnNoteRepo;
        this.ledgerRepo = ledgerRepo;
        this.stockRepo = stockRepo;
        this.stockTxnRepo = stockTxnRepo;
        this.refundService = refundService;
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
                throw new RuntimeException("Return source is required (DELIVERED / PENDING)");
            }

            BillItem billItem = billItemRepo.findById(req.getBillItemId())
                    .orElseThrow(() -> new RuntimeException("Bill item not found"));

            if (bill == null) {
                bill = billItem.getBill();
                note.setBill(bill);
                returnNoteRepo.save(note);
            } else if (!bill.getId().equals(billItem.getBill().getId())) {
                throw new RuntimeException("All return items must belong to the same bill");
            }

            BigDecimal requestedQty = req.getReturnedQuantity();
            if (requestedQty == null || requestedQty.signum() <= 0) {
                throw new RuntimeException("Returned quantity must be positive");
            }

            BigDecimal deliveredReturn = BigDecimal.ZERO;
            BigDecimal pendingReturn = BigDecimal.ZERO;

            if (req.getReturnSource() == ReturnSource.DELIVERED) {

                BigDecimal alreadyReturnedDelivered =
                        returnItemRepo.getTotalReturnedDeliveredQtyForBillItem(billItem.getId());

                BigDecimal maxAllowed =
                        billItem.getFulfilledQty().subtract(alreadyReturnedDelivered);

                if (requestedQty.compareTo(maxAllowed) > 0) {
                    throw new RuntimeException("Delivered return exceeds fulfilled quantity");
                }

                deliveredReturn = requestedQty;

            } else {

                BigDecimal alreadyReturnedPending =
                        returnItemRepo.getTotalReturnedPendingQtyForBillItem(billItem.getId());

                BigDecimal maxAllowed =
                        billItem.getPendingQty().subtract(alreadyReturnedPending);

                if (requestedQty.compareTo(maxAllowed) > 0) {
                    throw new RuntimeException("Pending return exceeds pending quantity");
                }

                pendingReturn = requestedQty;
            }

            BigDecimal lineAmount =
                    requestedQty.multiply(billItem.getPrice());

            returnedGross = returnedGross.add(lineAmount);

            if (deliveredReturn.signum() > 0) {

                Stock stock = stockRepo.findById(billItem.getItem().getId())
                        .orElseThrow(() -> new RuntimeException("Stock not found"));

                stock.setQuantity(stock.getQuantity().add(deliveredReturn));
                stockRepo.save(stock);

                StockTransaction txn = new StockTransaction();
                txn.setItem(billItem.getItem());
                txn.setTransactionType(StockTxnType.IN);
                txn.setQuantity(deliveredReturn);
                txn.setReferenceType(ReferenceType.RETURN);
                txn.setReferenceId(note.getId());
                stockTxnRepo.save(txn);
            }

            ReturnItem ri = new ReturnItem();
            ri.setReturnNote(note);
            ri.setBill(bill);
            ri.setBillItem(billItem);
            ri.setItem(billItem.getItem());
            ri.setReturnedDeliveredQty(deliveredReturn);
            ri.setReturnedPendingQty(pendingReturn);
            ri.setPrice(billItem.getPrice());

            returnItemRepo.save(ri);
        }

        BigDecimal billSubtotal = bill.getSubtotal();
        if (billSubtotal == null || billSubtotal.signum() <= 0) {
            throw new RuntimeException("Invalid bill subtotal");
        }

        BigDecimal billDiscount = bill.getDiscountAmount();

        BigDecimal returnPercentage =
                returnedGross.divide(billSubtotal, 6, RoundingMode.HALF_UP);

        BigDecimal clawedDiscount =
                billDiscount.multiply(returnPercentage)
                        .setScale(2, RoundingMode.HALF_UP);

        BigDecimal netReturn =
                returnedGross.subtract(clawedDiscount);

        if (netReturn.signum() <= 0) {
            throw new RuntimeException("Net return must be positive");
        }

        note.setReturnedGrossAmount(returnedGross);
        note.setClawedDiscountAmount(clawedDiscount);
        note.setNetReturnAmount(netReturn);

        returnNoteRepo.save(note);
        return note;
    }

    /* =====================================================
       STEP 2 — FINALIZE RETURN (RESIDUAL SAFE & IDEMPOTENT)
       ===================================================== */
    @Transactional
    public void finalizeReturn(UUID returnNoteId, User user) {

        ReturnNote note = returnNoteRepo.findById(returnNoteId)
                .orElseThrow(() -> new RuntimeException("Return note not found"));

        if (note.isFinalized()) {
            return;
        }

        Bill bill = note.getBill();

        // ===============================
        // 🔒 BILL CAP (SOURCE OF TRUTH)
        // ===============================
        BigDecimal billCap = bill.getTotalAmount();

        BigDecimal alreadyFinalized =
                returnNoteRepo.sumFinalizedNetReturnByBill(bill.getId());

        if (alreadyFinalized == null) {
            alreadyFinalized = BigDecimal.ZERO;
        }

        BigDecimal remainingCap =
                billCap.subtract(alreadyFinalized);

        // ===============================
        // 🧮 RESIDUAL ROUNDING ADJUSTMENT
        // ===============================
        BigDecimal tolerance = new BigDecimal("1.00");

        BigDecimal diff =
                note.getNetReturnAmount().subtract(remainingCap).abs();

        if (diff.compareTo(tolerance) <= 0) {

            note.setNetReturnAmount(remainingCap);

            BigDecimal adjustedClawback =
                    note.getReturnedGrossAmount().subtract(remainingCap);

            note.setClawedDiscountAmount(
                    adjustedClawback.max(BigDecimal.ZERO)
            );

            returnNoteRepo.save(note);

        } else if (note.getNetReturnAmount().compareTo(remainingCap) > 0) {
            throw new RuntimeException(
                    "Return exceeds remaining bill value. Max allowed: ₹" + remainingCap
            );
        }

        BigDecimal finalNet = note.getNetReturnAmount();
        Customer customer = bill.getCustomer();

        if (customer == null) {

            refundService.createRefundUnchecked(
                    bill,
                    finalNet,
                    "Refund against finalized return",
                    user
            );


        } else {

            BigDecimal due = ledgerRepo.getDueForBill(bill.getId());
            if (due == null) due = BigDecimal.ZERO;

            BigDecimal creditToDue = finalNet.min(due);

            if (creditToDue.signum() > 0) {
                CustomerLedger credit = new CustomerLedger();
                credit.setCustomer(customer);
                credit.setBill(bill);
                credit.setEntryType(LedgerType.CREDIT);
                credit.setReferenceType(ReferenceType.REFUND);
                credit.setAmount(creditToDue);
                ledgerRepo.save(credit);
            }

            BigDecimal refundAmount = finalNet.subtract(creditToDue);

            if (refundAmount.signum() > 0) {
                refundService.refundFromReturnNote(bill, refundAmount, user);
            }
        }

        note.setFinalized(true);
        note.setFinalizedAt(LocalDateTime.now());
        returnNoteRepo.save(note);
    }
}
