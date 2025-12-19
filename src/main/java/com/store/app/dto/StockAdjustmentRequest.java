package com.store.app.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class StockAdjustmentRequest {

    private UUID itemId;
    private BigDecimal quantity;
    private String reason;

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
