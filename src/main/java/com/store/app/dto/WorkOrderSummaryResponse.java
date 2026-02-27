package com.store.app.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class WorkOrderSummaryResponse {

    private final UUID workOrderId;
    private final String workOrderNumber;
    private final String customerName;
    private final String status;

    private final BigDecimal totalBilled;
    private final BigDecimal totalPaid;
    private final BigDecimal totalReturned;
    private final BigDecimal netDue;

    private final List<BillSummary> bills;

    public WorkOrderSummaryResponse(
            UUID workOrderId,
            String workOrderNumber,
            String customerName,
            String status,
            BigDecimal totalBilled,
            BigDecimal totalPaid,
            BigDecimal totalReturned,
            BigDecimal netDue,
            List<BillSummary> bills
    ) {
        this.workOrderId = workOrderId;
        this.workOrderNumber = workOrderNumber;
        this.customerName = customerName;
        this.status = status;
        this.totalBilled = totalBilled;
        this.totalPaid = totalPaid;
        this.totalReturned = totalReturned;
        this.netDue = netDue;
        this.bills = bills;
    }

    @Getter
    public static class BillSummary {
        private final UUID billId;
        private final String billCode;
        private final LocalDateTime billDate;
        private final BigDecimal effectiveTotal;
        private final BigDecimal amountPaid;
        private final BigDecimal due;

        public BillSummary(
                UUID billId,
                String billCode,
                LocalDateTime billDate,
                BigDecimal effectiveTotal,
                BigDecimal amountPaid,
                BigDecimal due
        ) {
            this.billId = billId;
            this.billCode = billCode;
            this.billDate = billDate;
            this.effectiveTotal = effectiveTotal;
            this.amountPaid = amountPaid;
            this.due = due;
        }
    }
}
