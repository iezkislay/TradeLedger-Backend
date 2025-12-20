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
import com.store.app.util.WhatsAppTemplates;
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
    private final NotificationService notificationService; // 🆕

    public FulfilmentService(
            BillItemRepository billItemRepo,
            StockRepository stockRepo,
            StockTransactionRepository stockTxnRepo,
            AuthService authService,
            NotificationService notificationService // 🆕
    ) {
        this.billItemRepo = billItemRepo;
        this.stockRepo = stockRepo;
        this.stockTxnRepo = stockTxnRepo;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    @Transactional
    public void fulfil(UUID billItemId, BigDecimal qty, User user) {

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

        bi.setFulfilledQty(bi.getFulfilledQty().add(qty));
        bi.setPendingQty(bi.getPendingQty().subtract(qty));

        if (bi.getPendingQty().signum() == 0) {
            bi.setFulfilmentStatus("FULL");
        } else {
            bi.setFulfilmentStatus("PARTIAL");
        }

        billItemRepo.save(bi);

        stock.setQuantity(stock.getQuantity().subtract(qty));
        stockRepo.save(stock);

        StockTransaction txn = new StockTransaction();
        txn.setItem(bi.getItem());
        txn.setTransactionType(StockTxnType.OUT);
        txn.setQuantity(qty);
        txn.setReferenceType(ReferenceType.BILL);
        txn.setReferenceId(bi.getBill().getId());

        stockTxnRepo.save(txn);

        // 📲 WhatsApp alert when fully fulfilled
        if (bi.getPendingQty().signum() == 0 &&
                bi.getBill().getCustomer() != null) {

            notificationService.sendWhatsApp(
                    bi.getBill().getCustomer().getMobile(),
                    WhatsAppTemplates.fulfilmentDone(bi)
            );
        }
    }

    public List<BillItem> getPendingFulfilments() {
        return billItemRepo.findByPendingQtyGreaterThan(BigDecimal.ZERO);
    }
}
