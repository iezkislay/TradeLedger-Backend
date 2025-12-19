package com.store.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemSalesRow(
        UUID itemId,
        String itemName,
        BigDecimal quantitySold,
        BigDecimal totalAmount
) {}
