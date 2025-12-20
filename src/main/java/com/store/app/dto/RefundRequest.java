package com.store.app.dto;

import com.store.app.enums.RefundMode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class RefundRequest {

    // 🧾 Bill for which refund is issued
    private UUID billId;

    // 💰 Refund amount
    private BigDecimal amount;

    // 💳 CASH / UPI / CREDIT
    private RefundMode refundMode;

    // 📝 Optional reason (customer return, cancellation, adjustment, etc.)
    private String reason;
}
