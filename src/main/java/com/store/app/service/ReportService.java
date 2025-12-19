package com.store.app.service;

import com.store.app.dto.*;
import com.store.app.enums.PaymentType;
import com.store.app.repository.BillRepository;
import com.store.app.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    // OLD SQL-heavy reports
    private final ReportRepository reportRepo;

    // NEW summary-based reports
    private final BillRepository billRepo;

    /* =========================
       🟢 OLD REPORTS (KEEP AS-IS)
       ========================= */

    // Existing daily sales (single date, row-based)
    public List<DailySalesReportRow> dailySales(LocalDate date) {
        return reportRepo.dailySales(date);
    }

    // Existing stock summary
    public List<StockSummaryRow> stockSummary() {
        return reportRepo.stockSummary();
    }

    // Existing item sales (requested quantity based)
    public List<ItemSalesRow> itemSales(LocalDate from, LocalDate to) {
        return reportRepo.itemSales(from, to);
    }

    /* =========================
       🆕 SUMMARY REPORTS (PHASE-2A)
       ========================= */

    // Daily sales summary (amount + count + payment split)
    public SalesSummaryDto dailySalesSummary(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return buildSummary(start, end);
    }

    // Monthly sales summary
    public SalesSummaryDto monthlySalesSummary(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = startDate.plusMonths(1).atStartOfDay();
        return buildSummary(start, end);
    }

    /* =========================
       🆕 ADVANCED REPORTS (PHASE-2C)
       ========================= */

    // Item-wise sales (fulfilled quantity)
    public List<ItemSalesReportDto> itemWiseSales(
            LocalDate from,
            LocalDate to
    ) {
        return reportRepo.itemWiseSales(from, to);
    }

    // Category-wise sales
    public List<CategorySalesReportDto> categoryWiseSales(
            LocalDate from,
            LocalDate to
    ) {
        return reportRepo.categoryWiseSales(from, to);
    }

    // Daily sales over a date range (fulfilled-based)
    public List<DailySalesReportDto> dailySalesRange(
            LocalDate from,
            LocalDate to
    ) {
        return reportRepo.dailySales(from, to);
    }

    /* =========================
       🔧 INTERNAL HELPER
       ========================= */

    private SalesSummaryDto buildSummary(
            LocalDateTime start,
            LocalDateTime end
    ) {

        BigDecimal total =
                billRepo.totalAmountBetween(start, end);

        long count =
                billRepo.countByCreatedAtBetween(start, end);

        Map<PaymentType, BigDecimal> paymentWise =
                new EnumMap<>(PaymentType.class);

        // Initialize all payment types
        for (PaymentType pt : PaymentType.values()) {
            paymentWise.put(pt, BigDecimal.ZERO);
        }

        // Populate actual totals
        List<Object[]> rows =
                billRepo.sumByPaymentType(start, end);

        for (Object[] r : rows) {
            paymentWise.put(
                    (PaymentType) r[0],
                    (BigDecimal) r[1]
            );
        }

        return new SalesSummaryDto(
                total,
                count,
                paymentWise
        );
    }
}
