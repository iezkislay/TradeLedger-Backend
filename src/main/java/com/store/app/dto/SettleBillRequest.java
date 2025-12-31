package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class SettleBillRequest {

    private BigDecimal amountPaid;     // cash / upi
    private BigDecimal adjustment;     // waiver / rounding
    private String paymentType;        // CASH / UPI
    private String adjustmentReason;   // optional

}
