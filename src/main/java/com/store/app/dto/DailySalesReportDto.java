package com.store.app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailySalesReportDto {

    private LocalDate date;
    private BigDecimal totalSales;

    public DailySalesReportDto(
            LocalDate date,
            BigDecimal totalSales
    ) {
        this.date = date;
        this.totalSales = totalSales;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }
}
