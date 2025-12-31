package com.store.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerListResponse(
        UUID id,
        String customerCode,
        String name,
        String mobile,
        String address,
        BigDecimal balance,
        LocalDateTime lastTransactionAt
) {}
