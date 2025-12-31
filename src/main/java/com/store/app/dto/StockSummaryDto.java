package com.store.app.dto;

import com.store.app.enums.BaseUnit;
import java.math.BigDecimal;
import java.util.UUID;

public class StockSummaryDto {

    private UUID itemId;
    private String name;
    private String itemCode;
    private String brand;
    private String category;
    private BaseUnit baseUnit;

    private BigDecimal quantity;
    private BigDecimal minStock;
    private BigDecimal costPrice;

    // 🔥 REQUIRED BY JPQL
    public StockSummaryDto(
            UUID itemId,
            String name,
            String itemCode,
            String brand,
            String category,
            BaseUnit baseUnit,
            BigDecimal quantity,
            BigDecimal minStock,
            BigDecimal costPrice
    ) {
        this.itemId = itemId;
        this.name = name;
        this.itemCode = itemCode;
        this.brand = brand;
        this.category = category;
        this.baseUnit = baseUnit;
        this.quantity = quantity;
        this.minStock = minStock;
        this.costPrice = costPrice;
    }

    // 🔢 Derived fields (NO DB QUERY)
    public BigDecimal getStockValue() {
        if (quantity == null || costPrice == null) return BigDecimal.ZERO;
        return quantity.multiply(costPrice);
    }

    public boolean isLowStock() {
        return minStock != null && quantity.compareTo(minStock) < 0;
    }

    // getters only (immutable DTO)
    public UUID getItemId() { return itemId; }
    public String getName() { return name; }
    public String getItemCode() { return itemCode; }
    public String getBrand() { return brand; }
    public String getCategory() { return category; }
    public BaseUnit getBaseUnit() { return baseUnit; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getMinStock() { return minStock; }
    public BigDecimal getCostPrice() { return costPrice; }
}
