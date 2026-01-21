package com.store.app.dto;

import java.math.BigDecimal;

public record LedgerBillSummary(
        BigDecimal debit,
        BigDecimal credit,
        BigDecimal returnCredit,
        BigDecimal adjustment,
        BigDecimal netBalance
) {}
