package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.StockAdjustmentRequest;
import com.store.app.entity.Stock;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final AuthService authService;

    // 🔧 Manual stock adjustment (OWNER)
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

    // 📦 Stock summary (all items)
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<Stock>>> getStockSummary() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        stockService.getStockSummary(),
                        "Stock summary fetched"
                )
        );
    }

    // 🟡 Low stock items
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<Stock>>> getLowStockItems() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        stockService.getLowStockItems(),
                        "Low stock items fetched"
                )
        );
    }
}
