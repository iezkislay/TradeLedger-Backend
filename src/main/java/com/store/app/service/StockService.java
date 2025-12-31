package com.store.app.service;

import com.store.app.dto.LowStockItemResponse;
import com.store.app.dto.StockAdjustmentRequest;
import com.store.app.dto.StockSummaryDto;
import com.store.app.entity.Item;
import com.store.app.entity.Stock;
import com.store.app.entity.StockTransaction;
import com.store.app.entity.User;
import com.store.app.enums.ReferenceType;
import com.store.app.enums.StockTxnType;
import com.store.app.repository.ItemRepository;
import com.store.app.repository.StockRepository;
import com.store.app.repository.StockTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockService {

    private final ItemRepository itemRepo;
    private final StockRepository stockRepo;
    private final StockTransactionRepository stockTxnRepo;
    private final AuthService authService;
    private final AuditService auditService;

    /* =====================================================
       📦 STOCK SUMMARY (ENTITY — INTERNAL USE)
       ===================================================== */

    /**
     * ⚠️ INTERNAL USE ONLY
     * Returns entities — NOT SAFE for controller JSON
     */
    public List<Stock> getStockSummary() {
        return stockRepo.findAll();
    }

    /* =====================================================
       🟢 STOCK SUMMARY (DTO — API SAFE)
       ===================================================== */

    /**
     * ✅ SAFE FOR CONTROLLER
     * Uses DTO projection → no lazy loading issues
     */
    public List<StockSummaryDto> getStockSummaryDto() {
        return stockRepo.fetchStockSummary();
    }

    /* =====================================================
       🟡 LOW STOCK ITEMS (DTO — API SAFE)
       ===================================================== */

    public List<LowStockItemResponse> getLowStockItems() {

        return stockRepo.findLowStockRaw().stream()
                .map(r -> {
                    LowStockItemResponse dto = new LowStockItemResponse();
                    dto.setItemId((UUID) r[0]);
                    dto.setItemName((String) r[1]);
                    dto.setAvailableQty((BigDecimal) r[2]);
                    dto.setMinStock((BigDecimal) r[3]);
                    return dto;
                })
                .toList();
    }

    /* =====================================================
       🔧 MANUAL STOCK ADJUSTMENT (OWNER ONLY)
       ===================================================== */

    @Transactional
    public void adjustStock(StockAdjustmentRequest request, User user) {

        authService.requireOwner(user);

        if (request.getQuantity() == null ||
                request.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("Adjustment quantity cannot be zero");
        }

        Item item = itemRepo.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Stock stock = stockRepo.findById(item.getId())
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        BigDecimal oldQty = stock.getQuantity();
        BigDecimal newQty = oldQty.add(request.getQuantity());

        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Stock cannot go negative");
        }

        stock.setQuantity(newQty);
        stockRepo.save(stock);

        // 🧾 STOCK ADJUST AUDIT
        auditService.log(
                "STOCK",
                item.getId(),
                "STOCK_ADJUST",
                oldQty.toString(),
                newQty.toString(),
                user
        );

        StockTransaction txn = new StockTransaction();
        txn.setItem(item);
        txn.setTransactionType(StockTxnType.ADJUST);
        txn.setQuantity(request.getQuantity().abs());
        txn.setReferenceType(ReferenceType.MANUAL);
        txn.setReferenceId(null);

        stockTxnRepo.save(txn);
    }
}
