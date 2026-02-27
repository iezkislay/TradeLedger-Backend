package com.store.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PendingFulfilmentItemView(
        UUID billItemId,
        String itemCode,
        String itemName,
        BigDecimal fulfilledQty,
        BigDecimal pendingQty,
        String status,
        LocalDateTime lastFulfilledAt
) {}

