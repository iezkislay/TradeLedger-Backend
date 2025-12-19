package com.store.app.controller;

import com.store.app.dto.*;
import com.store.app.service.ReportService;
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

    /* =========================
       🟢 OLD REPORTS (ROW-LEVEL)
       ========================= */

    // 📄 Daily detailed sales (single day, row-based)
    @GetMapping("/daily-sales")
    public ResponseEntity<ApiResponse<List<DailySalesReportRow>>> dailySales(
            @RequestParam LocalDate date
    ) {
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
    public ResponseEntity<ApiResponse<List<StockSummaryRow>>> stockSummary() {
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
            @RequestParam LocalDate to
    ) {
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

    // 🆕 Daily sales summary (amount + payment split)
    @GetMapping("/daily-sales-summary")
    public ResponseEntity<ApiResponse<SalesSummaryDto>> dailySalesSummary(
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        reportService.dailySalesSummary(date),
                        "Daily sales summary fetched"
                )
        );
    }

    // 🆕 Monthly sales summary
    @GetMapping("/monthly-sales-summary")
    public ResponseEntity<ApiResponse<SalesSummaryDto>> monthlySalesSummary(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        reportService.monthlySalesSummary(year, month),
                        "Monthly sales summary fetched"
                )
        );
    }

    /* =========================
       🆕 ADVANCED REPORTS (PHASE-2C)
       ========================= */

    // 📈 Item-wise sales (FULFILLED quantity)
    @GetMapping("/item-sales")
    public ResponseEntity<ApiResponse<List<ItemSalesReportDto>>> itemWiseSales(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
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
            @RequestParam LocalDate to
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        reportService.categoryWiseSales(from, to),
                        "Category-wise sales report"
                )
        );
    }

    // 📅 Daily sales range (fulfilled-based)
    @GetMapping("/daily-sales-range")
    public ResponseEntity<ApiResponse<List<DailySalesReportDto>>> dailySalesRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        reportService.dailySalesRange(from, to),
                        "Daily sales report"
                )
        );
    }
}
