package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class BillSummaryResponse {

    private UUID billId;
    private String billCode;

    private BigDecimal billAmount;
    private BigDecimal overriddenAmount;

    private BigDecimal returnedGrossValue;
    private BigDecimal returnedEffectiveValue;

    private BigDecimal refundedAmount;
    private BigDecimal refundableRemaining;

    private BigDecimal deliveredReturnValue;
    private BigDecimal pendingReturnValue;
}
