package com.store.app.dto;

import java.math.BigDecimal;

public interface CategorySalesReportDto {
    String getCategory();
    BigDecimal getQuantitySold();
    BigDecimal getTotalAmount();
}
