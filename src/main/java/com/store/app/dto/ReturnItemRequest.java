package com.store.app.dto;

import com.store.app.enums.ReturnSource;

import java.math.BigDecimal;
import java.util.UUID;

public class ReturnItemRequest {

    private UUID billItemId;
    private BigDecimal returnedQuantity;
    private ReturnSource returnSource; // DELIVERED | PENDING

    public UUID getBillItemId() {
        return billItemId;
    }

    public void setBillItemId(UUID billItemId) {
        this.billItemId = billItemId;
    }

    public BigDecimal getReturnedQuantity() {
        return returnedQuantity;
    }

    public void setReturnedQuantity(BigDecimal returnedQuantity) {
        this.returnedQuantity = returnedQuantity;
    }

    public ReturnSource getReturnSource() {
        return returnSource;
    }

    public void setReturnSource(ReturnSource returnSource) {
        this.returnSource = returnSource;
    }
}
