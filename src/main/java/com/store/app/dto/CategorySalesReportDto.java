package com.store.app.dto;

import java.math.BigDecimal;

public class CategorySalesReportDto {

    private String category;
    private BigDecimal quantitySold;
    private BigDecimal totalAmount;

    public CategorySalesReportDto(
            String category,
            BigDecimal quantitySold,
            BigDecimal totalAmount
    ) {
        this.category = category;
        this.quantitySold = quantitySold;
        this.totalAmount = totalAmount;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getQuantitySold() {
        return quantitySold;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
