package com.store.app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesReportRow(
        LocalDate date,
        Long totalBills,
        BigDecimal totalSales
) {}
