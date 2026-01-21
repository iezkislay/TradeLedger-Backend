package com.store.app.dto;

import java.math.BigDecimal;
import java.util.List;

public record GroupedReturnsResponse(
        List<ReturnNoteView> returns,
        BigDecimal returnedGrossTotal,
        BigDecimal returnedEffectiveTotal
) {}
