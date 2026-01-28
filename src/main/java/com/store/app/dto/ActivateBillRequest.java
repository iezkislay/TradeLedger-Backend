package com.store.app.dto;

import com.store.app.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ActivateBillRequest {

    private PaymentType paymentType;
    private BigDecimal amountPaid;

}
