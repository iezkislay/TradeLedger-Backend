package com.store.app.service;

import com.store.app.dto.DashboardKpiResponse;
import com.store.app.dto.DashboardResponse;
import com.store.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BillRepository billRepo;
    private final CustomerLedgerRepository ledgerRepo; // OLD (credit truth)
    private final CustomerRepository customerRepo;     // NEW (optional use)
    private final StockRepository stockRepo;
    private final RefundRepository refundRepo;

    /**
     * =========================
     * OLD METHOD (UNCHANGED)
     * =========================
     * Full dashboard payload
     */
    public DashboardResponse getDashboard() {

        DashboardResponse d = new DashboardResponse();
        LocalDate today = LocalDate.now();

        // 🔥 Sales
        d.setTodaySales(billRepo.todaySales(today));
        d.setMonthSales(billRepo.monthSales(today.getYear(), today.getMonthValue()));
        d.setPaymentSplit(billRepo.paymentSplit(today));
        d.setAvgBillValue(billRepo.avgBillValue(today));

        // 💳 Credit (LEDGER = source of truth)
        d.setTotalOutstanding(ledgerRepo.totalOutstanding());

        // 📦 Inventory
        d.setLowStockCount(stockRepo.lowStockCount());
        d.setTotalStockValue(stockRepo.totalStockValue());

        // 🔁 Refunds
        d.setTodayRefunds(refundRepo.todayRefunds(today));

        // 🚚 Pending fulfilment
        d.setPendingValue(billRepo.totalPendingValue());
        d.setPendingItems(billRepo.totalPendingItems());

        return d;
    }

    /**
     * =========================
     * NEW METHOD (ADDED)
     * =========================
     * Lightweight KPIs for dashboard widgets
     */
    public DashboardKpiResponse getKpis() {

        LocalDate today = LocalDate.now();

        BigDecimal todaySales = billRepo.todaySales(today);
        BigDecimal monthSales = billRepo.monthSales(today.getYear(), today.getMonthValue());
        BigDecimal avgBill = billRepo.avgBillValue(today);
        Map<String, BigDecimal> split = billRepo.paymentSplit(today);

        // 🔐 Prefer ledger for accuracy
        BigDecimal outstanding = ledgerRepo.totalOutstanding();
        BigDecimal pendingAmount = billRepo.totalPendingValue();

        Long lowStock = stockRepo.lowStockCount();
        BigDecimal stockValue = stockRepo.totalStockValue();

        return new DashboardKpiResponse(
                todaySales,
                monthSales,
                avgBill,
                split,
                outstanding,
                pendingAmount,
                lowStock,
                stockValue
        );
    }
}
