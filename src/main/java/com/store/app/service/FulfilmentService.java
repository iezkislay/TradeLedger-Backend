package com.store.app.service;

import com.store.app.entity.*;
import com.store.app.enums.ReferenceType;
import com.store.app.enums.StockTxnType;
import com.store.app.repository.BillItemRepository;
import com.store.app.repository.ReturnItemRepository;
import com.store.app.repository.StockRepository;
import com.store.app.repository.StockTransactionRepository;
import com.store.app.util.WhatsAppTemplates;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import static com.store.app.util.BillGuards.ensureActiveForOps;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class FulfilmentService {

    private final BillItemRepository billItemRepo;
    private final ReturnItemRepository returnItemRepo;
    private final StockRepository stockRepo;
    private final StockTransactionRepository stockTxnRepo;
    private final AuthService authService;
    private final NotificationService notificationService;

    public FulfilmentService(
            BillItemRepository billItemRepo,
            ReturnItemRepository returnItemRepo,
            StockRepository stockRepo,
            StockTransactionRepository stockTxnRepo,
            AuthService authService,
            NotificationService notificationService
    ) {
        this.billItemRepo = billItemRepo;
        this.returnItemRepo = returnItemRepo;
        this.stockRepo = stockRepo;
        this.stockTxnRepo = stockTxnRepo;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    @Transactional
    public void fulfil(UUID billItemId, BigDecimal qty, User user) {

        authService.requireBillingOrOwner(user);

        if (qty == null || qty.signum() <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        BillItem bi = billItemRepo.findById(billItemId)
                .orElseThrow(() -> new RuntimeException("Bill item not found"));

        Bill bill = bi.getBill();
        ensureActiveForOps(bill);


        BigDecimal returnedDeliveredQty =
                returnItemRepo.getTotalReturnedDeliveredQtyForBillItem(billItemId);
        BigDecimal returnedPendingQty =
                returnItemRepo.getTotalReturnedPendingQtyForBillItem(billItemId);

        if (returnedDeliveredQty == null) returnedDeliveredQty = BigDecimal.ZERO;
        if (returnedPendingQty == null) returnedPendingQty = BigDecimal.ZERO;

        BigDecimal returnedQty = returnedDeliveredQty.add(returnedPendingQty);

        BigDecimal orderedQty = bi.getQuantity();
        BigDecimal fulfilledQty = bi.getFulfilledQty();

        BigDecimal fulfilableQty = orderedQty
                .subtract(fulfilledQty)
                .subtract(returnedQty);

        if (fulfilableQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("No quantity available for fulfilment");
        }

        if (qty.compareTo(fulfilableQty) > 0) {
            throw new RuntimeException("Quantity exceeds fulfilable quantity");
        }

        Stock stock = stockRepo.findById(bi.getItem().getId())
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        if (stock.getQuantity().compareTo(qty) < 0) {
            throw new RuntimeException("Insufficient stock");
        }

        // =========================
        // APPLY FULFILMENT
        // =========================

        BigDecimal newFulfilledQty = fulfilledQty.add(qty);
        bi.setFulfilledQty(newFulfilledQty);

        BigDecimal newPendingQty = orderedQty
                .subtract(newFulfilledQty)
                .subtract(returnedQty);

        if (newPendingQty.signum() < 0) {
            newPendingQty = BigDecimal.ZERO;
        }

        bi.setPendingQty(newPendingQty);

        // ✅ FIXED STATUS LOGIC
        bi.setFulfilmentStatus(
                deriveStatus(bi, returnedQty)
        );

        billItemRepo.save(bi);

        // =========================
        // STOCK MOVEMENT
        // =========================

        stock.setQuantity(stock.getQuantity().subtract(qty));
        stockRepo.save(stock);

        StockTransaction txn = new StockTransaction();
        txn.setItem(bi.getItem());
        txn.setTransactionType(StockTxnType.OUT);
        txn.setQuantity(qty);
        txn.setReferenceType(ReferenceType.BILL);
        txn.setReferenceId(bi.getBill().getId());

        stockTxnRepo.save(txn);

        // =========================
        // NOTIFICATION
        // =========================

        BigDecimal netQty = orderedQty.subtract(returnedQty);

        if (newFulfilledQty.compareTo(netQty) == 0 &&
                bi.getBill().getCustomer() != null) {

            notificationService.sendWhatsApp(
                    bi.getBill().getCustomer().getMobile(),
                    WhatsAppTemplates.fulfilmentDone(bi)
            );
        }
    }

    /**
     * ✅ SINGLE SOURCE OF TRUTH FOR FULFILMENT STATUS
     */
    private String deriveStatus(BillItem bi, BigDecimal returnedQty) {

        BigDecimal ordered = bi.getQuantity();
        BigDecimal fulfilled = bi.getFulfilledQty();
        BigDecimal fulfillable = ordered.subtract(returnedQty);

        if (returnedQty.compareTo(ordered) == 0) {
            return "RETURNED";
        }
        if (fulfilled.signum() == 0) {
            return "PENDING";
        }
        if (fulfilled.compareTo(fulfillable) < 0) {
            return "PARTIAL";
        }
        return "FULL";
    }

    /**
     * Pending fulfilments are computed dynamically
     */
    public List<BillItem> getPendingFulfilments() {
        return billItemRepo.findPendingFulfilments();
    }
}
