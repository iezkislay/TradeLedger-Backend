package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Setter
@Getter
public class DashboardResponse {

    // 🔥 Sales
    private BigDecimal todaySales;
    private BigDecimal monthSales;
    private Map<String, BigDecimal> paymentSplit;
    private BigDecimal avgBillValue;

    // 💰 Ledger-based KPIs (NEW)
    private BigDecimal cashCollected;   // actual cash received
    private BigDecimal waivedAmount;    // goodwill / rounding loss

    // 💳 Outstanding (ledger truth)
    private BigDecimal totalOutstanding;

    // 📦 Inventory
    private Long lowStockCount;
    private BigDecimal totalStockValue;

    // 🔁 Returns / Refunds
    private BigDecimal todayRefunds;

    // 🚚 Pending fulfilment
    private BigDecimal pendingValue;
    private Long pendingItems;
}
