package com.store.app.dto;

import com.store.app.enums.PaymentType;
import java.math.BigDecimal;
import java.util.Map;

public record SalesSummaryDto(
        BigDecimal totalAmount,
        long billCount,
        Map<PaymentType, BigDecimal> paymentWiseTotal
) {}
