package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BillOverrideRequest {

    private BigDecimal overriddenAmount;
    private String reason;
}
