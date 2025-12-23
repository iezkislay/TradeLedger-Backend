package com.store.app.dto;

import java.math.BigDecimal;
import java.util.Map;

public class DashboardResponse {

    // 🔥 Sales
    private BigDecimal todaySales;
    private BigDecimal monthSales;
    private Map<String, BigDecimal> paymentSplit;
    private BigDecimal avgBillValue;

    // 💳 Credit
    private BigDecimal totalOutstanding;

    // 📦 Inventory
    private Long lowStockCount;
    private BigDecimal totalStockValue;

    // 🔁 Returns / Refunds
    private BigDecimal todayRefunds;

    // 🚚 Pending fulfilment
    private BigDecimal pendingValue;
    private Long pendingItems;

    /* ===== getters & setters ===== */

    public BigDecimal getTodaySales() { return todaySales; }
    public void setTodaySales(BigDecimal todaySales) { this.todaySales = todaySales; }

    public BigDecimal getMonthSales() { return monthSales; }
    public void setMonthSales(BigDecimal monthSales) { this.monthSales = monthSales; }

    public Map<String, BigDecimal> getPaymentSplit() { return paymentSplit; }
    public void setPaymentSplit(Map<String, BigDecimal> paymentSplit) { this.paymentSplit = paymentSplit; }

    public BigDecimal getAvgBillValue() { return avgBillValue; }
    public void setAvgBillValue(BigDecimal avgBillValue) { this.avgBillValue = avgBillValue; }

    public BigDecimal getTotalOutstanding() { return totalOutstanding; }
    public void setTotalOutstanding(BigDecimal totalOutstanding) { this.totalOutstanding = totalOutstanding; }

    public Long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(Long lowStockCount) { this.lowStockCount = lowStockCount; }

    public BigDecimal getTotalStockValue() { return totalStockValue; }
    public void setTotalStockValue(BigDecimal totalStockValue) { this.totalStockValue = totalStockValue; }

    public BigDecimal getTodayRefunds() { return todayRefunds; }
    public void setTodayRefunds(BigDecimal todayRefunds) { this.todayRefunds = todayRefunds; }

    public BigDecimal getPendingValue() { return pendingValue; }
    public void setPendingValue(BigDecimal pendingValue) { this.pendingValue = pendingValue; }

    public Long getPendingItems() { return pendingItems; }
    public void setPendingItems(Long pendingItems) { this.pendingItems = pendingItems; }
}
