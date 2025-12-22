package com.store.app.dto;

import java.math.BigDecimal;

public class ItemSalesReportDto {

    private String itemName;
    private String category;
    private BigDecimal quantitySold;
    private BigDecimal totalAmount;

    public ItemSalesReportDto(
            String itemName,
            String category,
            BigDecimal quantitySold,
            BigDecimal totalAmount
    ) {
        this.itemName = itemName;
        this.category = category;
        this.quantitySold = quantitySold;
        this.totalAmount = totalAmount;
    }

    public String getItemName() {
        return itemName;
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
