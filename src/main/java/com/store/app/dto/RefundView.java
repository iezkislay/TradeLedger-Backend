package com.store.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RefundView(
        UUID refundId,
        BigDecimal amount,
        String refundMode,
        String reason,
        LocalDateTime createdAt
) {}
