package com.store.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BillItemResponse(
        UUID billItemId,

        String itemCode,
        String itemName,

        BigDecimal orderedQty,
        BigDecimal fulfilledQty,
        BigDecimal pendingQty,

        BigDecimal returnedQty,   // 🔥 DERIVED
        BigDecimal netQty,        // 🔥 DERIVED

        BigDecimal rate,
        BigDecimal amount,

        String status
) {}
