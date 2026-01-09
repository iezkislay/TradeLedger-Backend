package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.LowStockItemResponse;
import com.store.app.dto.StockAdjustmentRequest;
import com.store.app.dto.StockSummaryDto;
import com.store.app.entity.Stock;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final AuthService authService;

    /* =====================================================
       🔧 MANUAL STOCK ADJUSTMENT (EXISTING — STRICT)
       ⚠️ DO NOT TOUCH (LEGACY)
       ===================================================== */
    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<String>> adjustStock(
            @RequestBody StockAdjustmentRequest request,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        stockService.adjustStock(request, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Stock adjusted successfully")
        );
    }

    /* =====================================================
       🆕 SAFE STOCK ADJUSTMENT (AUTO-CREATE STOCK)
       ===================================================== */
    @PostMapping("/{itemId}/adjust")
    public ResponseEntity<ApiResponse<String>> adjustStockSafe(
            @PathVariable UUID itemId,
            @RequestBody StockAdjustmentRequest request,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        stockService.adjustStockSafe(itemId, request, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Stock adjusted successfully")
        );
    }

    /* =====================================================
       📦 STOCK SUMMARY (DTO — API SAFE)
       ===================================================== */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<StockSummaryDto>>> getStockSummary() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        stockService.getStockSummaryDto(),
                        "Stock summary fetched"
                )
        );
    }

    /* =====================================================
       🟡 LOW STOCK ITEMS (DTO — API SAFE)
       ===================================================== */
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<LowStockItemResponse>>> getLowStockItems() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        stockService.getLowStockItems(),
                        "Low stock items fetched"
                )
        );
    }

    /* =====================================================
       ⚠️ INTERNAL / ADMIN — ENTITY ACCESS
       ===================================================== */
    @GetMapping("/summary-raw")
    public ResponseEntity<ApiResponse<List<Stock>>> getStockSummaryRaw() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        stockService.getStockSummary(),
                        "Raw stock summary fetched"
                )
        );
    }
}
