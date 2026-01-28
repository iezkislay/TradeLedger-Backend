package com.store.app.dto;

import com.store.app.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ActivateEstimateRequest {
    private PaymentType paymentType;
    private BigDecimal amountPaid;
}
