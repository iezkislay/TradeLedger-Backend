package com.store.app.controller;

import com.store.app.dto.*;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final AuthService authService;

    /* =========================
       🟢 OLD REPORTS (ROW-LEVEL)
       ========================= */

    // 📄 Daily detailed sales (single day, row-based)
    @GetMapping("/daily-sales")
    public ResponseEntity<ApiResponse<List<DailySalesReportRow>>> dailySales(
            @RequestParam LocalDate date,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        reportService.dailySales(date),
                        "Daily sales fetched"
                )
        );
    }

    // 📦 Stock summary
    @GetMapping("/stock-summary")
    public ResponseEntity<ApiResponse<List<StockSummaryRow>>> stockSummary(
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        reportService.stockSummary(),
                        "Stock summary fetched"
                )
        );
    }

    // 📊 Item-wise sales (OLD – requested qty based)
    @GetMapping("/item-sales-old")
    public ResponseEntity<ApiResponse<List<ItemSalesRow>>> itemSalesOld(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        reportService.itemSales(from, to),
                        "Item sales fetched (legacy)"
                )
        );
    }

    /* =========================
       🆕 SUMMARY REPORTS
       ========================= */

    @GetMapping("/daily-sales-summary")
    public ResponseEntity<ApiResponse<SalesSummaryDto>> dailySalesSummary(
            @RequestParam LocalDate date,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        reportService.dailySalesSummary(date),
                        "Daily sales summary fetched"
                )
        );
    }

    @GetMapping("/monthly-sales-summary")
    public ResponseEntity<ApiResponse<SalesSummaryDto>> monthlySalesSummary(
            @RequestParam int year,
            @RequestParam int month,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        reportService.monthlySalesSummary(year, month),
                        "Monthly sales summary fetched"
                )
        );
    }

    /* =========================
       🆕 ADVANCED REPORTS
       ========================= */

    // 📈 Item-wise sales (FULFILLED quantity)
    @GetMapping("/item-sales")
    public ResponseEntity<ApiResponse<List<ItemSalesReportDto>>> itemWiseSales(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        reportService.itemWiseSales(from, to),
                        "Item-wise sales report"
                )
        );
    }

    // 🗂 Category-wise sales
    @GetMapping("/category-sales")
    public ResponseEntity<ApiResponse<List<CategorySalesReportDto>>> categoryWiseSales(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        reportService.categoryWiseSales(from, to),
                        "Category-wise sales report"
                )
        );
    }

    // 📅 Daily sales range
    @GetMapping("/daily-sales-range")
    public ResponseEntity<ApiResponse<List<DailySalesReportDto>>> dailySalesRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        reportService.dailySalesRange(from, to),
                        "Daily sales report"
                )
        );
    }
}
