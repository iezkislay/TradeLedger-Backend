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

    public List<DailySalesReportRow> dailySales(LocalDate date) {
        return reportRepo.dailySales(date);
    }

    public List<StockSummaryRow> stockSummary() {
        return reportRepo.stockSummary();
    }

    public List<ItemSalesRow> itemSales(LocalDate from, LocalDate to) {
        return reportRepo.itemSales(from, to);
    }

    /* =========================
       🆕 SUMMARY REPORTS (PHASE-2A)
       ========================= */

    public SalesSummaryDto dailySalesSummary(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return buildSummary(start, end);
    }

    public SalesSummaryDto monthlySalesSummary(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = startDate.plusMonths(1).atStartOfDay();
        return buildSummary(start, end);
    }

    /* =========================
       🆕 ADVANCED REPORTS (PHASE-2C)
       ========================= */

    // ✅ FIXED: Item-wise sales (fulfilled qty)
    public List<ItemSalesReportDto> itemWiseSales(
            LocalDate from,
            LocalDate to
    ) {
        return reportRepo.itemWiseSalesRaw(from, to)
                .stream()
                .map(r -> new ItemSalesReportDto(
                        (String) r[0],           // itemName
                        (String) r[1],           // category
                        (BigDecimal) r[2],       // quantitySold
                        (BigDecimal) r[3]        // totalAmount
                ))
                .toList();
    }

    // ✅ FIXED: Category-wise sales
    public List<CategorySalesReportDto> categoryWiseSales(
            LocalDate from,
            LocalDate to
    ) {
        return reportRepo.categoryWiseSalesRaw(from, to)
                .stream()
                .map(r -> new CategorySalesReportDto(
                        (String) r[0],           // category
                        (BigDecimal) r[1],       // quantitySold
                        (BigDecimal) r[2]        // totalAmount
                ))
                .toList();
    }

    // ✅ FIXED: Daily sales range
    public List<DailySalesReportDto> dailySalesRange(
            LocalDate from,
            LocalDate to
    ) {
        return reportRepo.dailySalesRangeRaw(from, to)
                .stream()
                .map(r -> new DailySalesReportDto(
                        ((java.sql.Date) r[0]).toLocalDate(),
                        (BigDecimal) r[1]
                ))
                .toList();
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
