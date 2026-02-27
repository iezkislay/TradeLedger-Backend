package com.store.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PendingFulfilmentRow(
        UUID billItemId,
        UUID billId,
        String billCode,
        String customerName,
        String itemCode,
        String itemName,
        BigDecimal fulfilledQty,
        BigDecimal pendingQty,
        String status,
        LocalDateTime lastFulfilledAt
) {}
