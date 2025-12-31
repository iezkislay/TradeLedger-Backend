package com.store.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * READ-ONLY projection for customer balance summary.
 * Used for:
 * - Dashboard listings
 * - Paginated customer balance views
 * - Reports / analytics
 * ⚠️ No business logic here.
 * ⚠️ Must remain immutable.
 */
public record CustomerBalanceView(
        UUID customerId,
        String customerCode,
        String name,
        String mobile,
        String address,
        BigDecimal balance
) {
}
