package com.store.app.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReturnsResponse(
        List<ReturnRowView> rows,
        BigDecimal returnedGrossTotal,
        BigDecimal returnedEffectiveTotal
) {}
