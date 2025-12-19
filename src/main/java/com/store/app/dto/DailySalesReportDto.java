package com.store.app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailySalesReportDto {
    LocalDate getDate();
    BigDecimal getTotalSales();
}
