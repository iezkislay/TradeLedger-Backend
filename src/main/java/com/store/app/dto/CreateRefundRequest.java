package com.store.app.dto;

import com.store.app.enums.RefundMode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateRefundRequest {

    private UUID returnId;
    private BigDecimal amount;
    private RefundMode refundMode;
    private String reason;
}
