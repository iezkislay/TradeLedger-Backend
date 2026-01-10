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

    /**
     * Final bill total (cap for returns & refunds)
     */
    private BigDecimal totalAmount;

    /**
     * Overridden bill amount (if any)
     */
    private BigDecimal overriddenAmount;

    /**
     * Returns (ERP truth)
     */
    private BigDecimal returnedGrossValue;
    private BigDecimal returnedEffectiveValue;

    /**
     * Refunds issued (cash out)
     */
    private BigDecimal refundedAmount;

    /**
     * Remaining refundable value
     */
    private BigDecimal refundableRemaining;

    /**
     * Return split
     */
    private BigDecimal deliveredReturnValue;
    private BigDecimal pendingReturnValue;
}
