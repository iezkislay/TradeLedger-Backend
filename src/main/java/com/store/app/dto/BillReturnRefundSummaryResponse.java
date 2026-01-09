package com.store.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class BillReturnRefundSummaryResponse {

    private UUID billId;

    private BigDecimal totalReturnValue;
    private BigDecimal totalRefunded;
    private BigDecimal remainingRefundable;

    private List<ReturnedItemRow> items;

    @Getter
    @AllArgsConstructor
    public static class ReturnedItemRow {
        private String itemName;
        private String baseUnit;
        private BigDecimal returnedQty;
        private BigDecimal price;
        private BigDecimal amount;
    }
}
