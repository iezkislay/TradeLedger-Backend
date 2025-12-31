package com.store.app.service;

import com.store.app.dto.DashboardKpiResponse;
import com.store.app.dto.DashboardResponse;
import com.store.app.dto.DashboardSummaryResponse;
import com.store.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BillRepository billRepo;
    private final BillItemRepository billItemRepo;
    private final CustomerLedgerRepository ledgerRepo;
    private final StockRepository stockRepo;
    private final RefundRepository refundRepo;

    /**
     * =========================
     * FULL DASHBOARD (LEGACY VIEW)
     * =========================
     */
    public DashboardResponse getDashboard() {

        DashboardResponse d = new DashboardResponse();
        LocalDate today = LocalDate.now();

        // 🔥 SALES (BILL-BASED)
        d.setTodaySales(billRepo.todaySales(today));
        d.setMonthSales(billRepo.monthSales(today.getYear(), today.getMonthValue()));
        d.setPaymentSplit(billRepo.paymentSplit(today));
        d.setAvgBillValue(billRepo.avgBillValue(today));

        // 💰 LEDGER KPIs (SOURCE OF TRUTH)
        d.setCashCollected(ledgerRepo.totalCashReceived());
        d.setTotalOutstanding(ledgerRepo.totalOutstandingLedger());
        d.setWaivedAmount(ledgerRepo.totalWaived());

        // 📦 INVENTORY
        d.setLowStockCount(stockRepo.lowStockCount());
        d.setTotalStockValue(stockRepo.totalStockValue());

        // 🔁 REFUNDS
        d.setTodayRefunds(refundRepo.todayRefunds(today));

        // 🚚 PENDING FULFILMENT
        d.setPendingValue(billRepo.totalPendingValue());
        d.setPendingItems(billRepo.totalPendingItems());

        return d;
    }

    /**
     * =========================
     * KPI DASHBOARD (WIDGETS)
     * =========================
     */
    public DashboardKpiResponse getKpis() {

        LocalDate today = LocalDate.now();

        // 🔥 SALES
        BigDecimal todaySales = billRepo.todaySales(today);
        BigDecimal monthSales =
                billRepo.monthSales(today.getYear(), today.getMonthValue());
        BigDecimal avgBill = billRepo.avgBillValue(today);
        Map<String, BigDecimal> split = billRepo.paymentSplit(today);

        // 💰 LEDGER KPIs
        BigDecimal cashCollected = ledgerRepo.totalCashReceived();
        BigDecimal outstanding = ledgerRepo.totalOutstandingLedger();

        // 📦 INVENTORY
        Long lowStock = stockRepo.lowStockCount();
        BigDecimal stockValue = stockRepo.totalStockValue();

        // ✅ FIX: constructor matches EXACTLY (8 params)
        return new DashboardKpiResponse(
                todaySales,
                monthSales,
                avgBill,
                split,
                cashCollected,
                outstanding,
                lowStock,
                stockValue
        );
    }

    /**
     * =========================
     * DASHBOARD SUMMARY (RANGE)
     * =========================
     */
    public DashboardSummaryResponse getDashboardSummary(
            LocalDate from,
            LocalDate to
    ) {

        LocalDate today = LocalDate.now();

        // 🔥 SALES KPIs
        BigDecimal todaySales = billRepo.todaySales(today);
        BigDecimal monthSales =
                billRepo.monthSales(today.getYear(), today.getMonthValue());
        BigDecimal avgBill = billRepo.avgBillValue(from, to);

        DashboardSummaryResponse.Sales sales =
                new DashboardSummaryResponse.Sales(
                        todaySales,
                        monthSales,
                        avgBill
                );

        // 💳 PAYMENT SPLIT
        List<DashboardSummaryResponse.NamedValue> paymentSplit =
                billRepo.paymentSplit(from, to)
                        .stream()
                        .map(r -> new DashboardSummaryResponse.NamedValue(
                                r[0].toString(),
                                (BigDecimal) r[1]
                        ))
                        .toList();

        // 📈 SALES TREND
        List<DashboardSummaryResponse.SalesTrend> salesTrend =
                billRepo.salesTrend(from, to)
                        .stream()
                        .map(r -> new DashboardSummaryResponse.SalesTrend(
                                (LocalDate) r[0],
                                (BigDecimal) r[1]
                        ))
                        .toList();

        // 🏆 TOP ITEMS
        List<DashboardSummaryResponse.TopItem> topItems =
                billItemRepo.topItems(from, to)
                        .stream()
                        .limit(5)
                        .map(r -> new DashboardSummaryResponse.TopItem(
                                (String) r[0],
                                (BigDecimal) r[1]
                        ))
                        .toList();

        return new DashboardSummaryResponse(
                sales,
                paymentSplit,
                salesTrend,
                topItems
        );
    }
}
