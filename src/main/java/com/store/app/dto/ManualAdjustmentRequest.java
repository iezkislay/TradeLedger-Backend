package com.store.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ManualAdjustmentRequest {
    private UUID billId;
    private BigDecimal amount;
    private String reason;

    public UUID getBillId() { return billId; }
    public void setBillId(UUID billId) { this.billId = billId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
