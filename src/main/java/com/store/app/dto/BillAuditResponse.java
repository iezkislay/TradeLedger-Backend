package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class BillAuditResponse {

    // Bill
    private UUID billId;
    private String billCode;
    private String state;

    // Ledger
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal returnCredit;
    private BigDecimal adjustment;
    private BigDecimal netBalance;

    // Goods
    private BigDecimal totalOrderedQty;
    private BigDecimal totalFulfilledQty;
    private BigDecimal totalReturnedQty;

    // Returns
    private BigDecimal returnedGrossTotal;
    private BigDecimal returnedEffectiveTotal;

    // Refunds
    private BigDecimal refundedTotal;

    public BillAuditResponse(
            UUID billId,
            String billCode,
            String state,
            BigDecimal debit,
            BigDecimal credit,
            BigDecimal returnCredit,
            BigDecimal adjustment,
            BigDecimal netBalance,
            BigDecimal totalOrderedQty,
            BigDecimal totalFulfilledQty,
            BigDecimal totalReturnedQty,
            BigDecimal returnedGrossTotal,
            BigDecimal returnedEffectiveTotal,
            BigDecimal refundedTotal
    ) {
        this.billId = billId;
        this.billCode = billCode;
        this.state = state;
        this.debit = debit;
        this.credit = credit;
        this.returnCredit = returnCredit;
        this.adjustment = adjustment;
        this.netBalance = netBalance;
        this.totalOrderedQty = totalOrderedQty;
        this.totalFulfilledQty = totalFulfilledQty;
        this.totalReturnedQty = totalReturnedQty;
        this.returnedGrossTotal = returnedGrossTotal;
        this.returnedEffectiveTotal = returnedEffectiveTotal;
        this.refundedTotal = refundedTotal;
    }

    // getters
}
