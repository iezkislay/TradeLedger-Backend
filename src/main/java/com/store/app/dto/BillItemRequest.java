package com.store.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class BillItemRequest {

    private UUID itemId;
    private BigDecimal quantity;
    private BigDecimal price;

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
