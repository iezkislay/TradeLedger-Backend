package com.store.app.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ReturnNoteView(
        UUID returnId,
        boolean finalized,

        BigDecimal grossTotal,
        BigDecimal effectiveTotal,

        BigDecimal alreadyRefunded,
        BigDecimal refundableRemaining,

        boolean residualAdjusted,      // 🔥 NEW
        String adjustmentNote,         // 🔥 NEW

        List<ReturnItemView> items
) {}
