package com.store.app.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class GstSummaryRow {

    private BigDecimal gstRate;
    private BigDecimal taxable;
    private BigDecimal cgst;
    private BigDecimal sgst;

    public GstSummaryRow(
            BigDecimal gstRate,
            BigDecimal taxable,
            BigDecimal cgst,
            BigDecimal sgst
    ) {
        this.gstRate = gstRate;
        this.taxable = taxable;
        this.cgst = cgst;
        this.sgst = sgst;
    }

}