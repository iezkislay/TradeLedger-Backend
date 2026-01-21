package com.store.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReturnItemView(
        UUID billItemId,
        String itemCode,
        String itemName,

        BigDecimal quantity,
        BigDecimal grossAmount,
        BigDecimal effectiveAmount,

        String returnType,
        LocalDateTime createdAt
) {}
