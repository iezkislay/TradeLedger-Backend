package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class LowStockItemResponse {

    private UUID itemId;
    private String itemName;
    private BigDecimal availableQty;
    private BigDecimal minStock;

    public LowStockItemResponse() {
    }

    public LowStockItemResponse(
            UUID itemId,
            String itemName,
            BigDecimal availableQty,
            BigDecimal minStock
    ) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.availableQty = availableQty;
        this.minStock = minStock;
    }

    @Override
    public String toString() {
        return "LowStockItemResponse{" +
                "itemId=" + itemId +
                ", itemName='" + itemName + '\'' +
                ", availableQty=" + availableQty +
                ", minStock=" + minStock +
                '}';
    }
}
