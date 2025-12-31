package com.store.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * RESPONSE DTO — BILLING ONLY
 * Used for item search and billing UI.
 * Immutable, read-only projection.
 */
public record ItemSearchResponse(
        UUID id,
        String itemCode,
        String name,
        String baseUnit,
        BigDecimal sellingPrice,
        BigDecimal availableStock
) {}
