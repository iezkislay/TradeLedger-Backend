package com.store.app.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardKpiResponse(
        BigDecimal todaySales,
        BigDecimal monthSales,
        BigDecimal avgBillValue,
        Map<String, BigDecimal> paymentSplit,

        BigDecimal totalOutstanding,
        BigDecimal pendingAmount,

        Long lowStockCount,
        BigDecimal totalStockValue
) {}
