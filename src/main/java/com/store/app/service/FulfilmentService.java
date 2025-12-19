package com.store.app.service;

import com.store.app.entity.BillItem;
import com.store.app.entity.Stock;
import com.store.app.entity.StockTransaction;
import com.store.app.entity.User;
import com.store.app.enums.ReferenceType;
import com.store.app.enums.StockTxnType;
import com.store.app.repository.BillItemRepository;
import com.store.app.repository.StockRepository;
import com.store.app.repository.StockTransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class FulfilmentService {

    private final BillItemRepository billItemRepo;
    private final StockRepository stockRepo;
    private final StockTransactionRepository stockTxnRepo;
    private final AuthService authService;

    public FulfilmentService(
            BillItemRepository billItemRepo,
            StockRepository stockRepo,
            StockTransactionRepository stockTxnRepo,
            AuthService authService
    ) {
        this.billItemRepo = billItemRepo;
        this.stockRepo = stockRepo;
        this.stockTxnRepo = stockTxnRepo;
        this.authService = authService;
    }

    // 🔄 Fulfil pending quantity (OWNER + BILLING)
    @Transactional
    public void fulfil(UUID billItemId, BigDecimal qty, User user) {

        // 🔒 RBAC
        authService.requireBillingOrOwner(user);

        BillItem bi = billItemRepo.findById(billItemId)
                .orElseThrow(() -> new RuntimeException("Bill item not found"));

        if (qty.compareTo(bi.getPendingQty()) > 0) {
            throw new RuntimeException("Quantity exceeds pending");
        }

        Stock stock = stockRepo.findById(bi.getItem().getId())
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        if (stock.getQuantity().compareTo(qty) < 0) {
            throw new RuntimeException("Insufficient stock");
        }

        // 1️⃣ Update bill item quantities
        bi.setFulfilledQty(bi.getFulfilledQty().add(qty));
        bi.setPendingQty(bi.getPendingQty().subtract(qty));

        if (bi.getPendingQty().compareTo(BigDecimal.ZERO) == 0) {
            bi.setFulfilmentStatus("FULL");
        } else {
            bi.setFulfilmentStatus("PARTIAL");
        }

        billItemRepo.save(bi);

        // 2️⃣ Update stock
        stock.setQuantity(stock.getQuantity().subtract(qty));
        stockRepo.save(stock);

        // 3️⃣ Stock transaction
        StockTransaction txn = new StockTransaction();
        txn.setItem(bi.getItem());
        txn.setTransactionType(StockTxnType.OUT);
        txn.setQuantity(qty);
        txn.setReferenceType(ReferenceType.BILL);
        txn.setReferenceId(bi.getBill().getId());

        stockTxnRepo.save(txn);
    }

    // 📊 Pending fulfilment report
    public List<BillItem> getPendingFulfilments() {
        return billItemRepo.findByPendingQtyGreaterThan(BigDecimal.ZERO);
    }
}
