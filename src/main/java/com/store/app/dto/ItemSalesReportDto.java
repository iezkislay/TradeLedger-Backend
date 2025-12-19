package com.store.app.dto;

import java.math.BigDecimal;

public interface ItemSalesReportDto {
    String getItemName();
    String getCategory();
    BigDecimal getQuantitySold();
    BigDecimal getTotalAmount();
}
