package com.store.app.dto;

import java.util.UUID;
import java.util.List;

public record PendingFulfilmentBillGroupView(
        UUID billId,
        String billCode,
        String customerName,
        List<PendingFulfilmentItemView> items
) {}
