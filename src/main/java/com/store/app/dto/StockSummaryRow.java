package com.store.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record StockSummaryRow(
        UUID itemId,
        String itemName,
        BigDecimal quantity
) {}
