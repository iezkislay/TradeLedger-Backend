package com.store.app.dto;

import com.store.app.enums.PaymentType;
import java.util.List;
import java.util.UUID;

public class CreateBillRequest {

    private PaymentType paymentType;
    private UUID customerId;
    private List<BillItemRequest> items;

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public List<BillItemRequest> getItems() {
        return items;
    }

    public void setItems(List<BillItemRequest> items) {
        this.items = items;
    }
}
