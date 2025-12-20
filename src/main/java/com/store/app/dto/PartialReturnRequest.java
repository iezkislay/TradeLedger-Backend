package com.store.app.dto;

import com.store.app.enums.ReturnType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public class PartialReturnRequest {

    @NotNull
    private UUID billItemId;

    @NotNull
    private ReturnType returnType;

    @NotNull
    @Positive
    private BigDecimal quantity;

    private String reason;

    // ---- Getters & Setters ----

    public UUID getBillItemId() {
        return billItemId;
    }

    public void setBillItemId(UUID billItemId) {
        this.billItemId = billItemId;
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public void setReturnType(ReturnType returnType) {
        this.returnType = returnType;
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
