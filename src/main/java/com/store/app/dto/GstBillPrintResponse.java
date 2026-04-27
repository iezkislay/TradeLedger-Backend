package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class GstBillPrintResponse {

    // getters + setters
    private String billNumber;
    private LocalDateTime billDate;

    private String customerName;
    private String customerMobile;
    private String customerAddress;
    private String customerGstin;
    private String placeOfSupply;
    private String baseUnit;

    private List<PrintItem> items;

    private BigDecimal taxableAmount;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal totalAmount;

    private List<GstSummaryRow> gstSummary;

    public static class PrintItem {
        public String name;
        public String hsn;
        public BigDecimal qty;
        public BigDecimal rate;
        public BigDecimal taxable;
        public BigDecimal cgst;
        public BigDecimal sgst;
        public BigDecimal amount;
        public String baseUnit;

        public PrintItem(
                String name,
                String hsn,
                BigDecimal qty,
                BigDecimal rate,
                BigDecimal taxable,
                BigDecimal cgst,
                BigDecimal sgst,
                BigDecimal amount,
                String baseUnit
        ) {
            this.name = name;
            this.hsn = hsn;
            this.qty = qty;
            this.rate = rate;
            this.taxable = taxable;
            this.cgst = cgst;
            this.sgst = sgst;
            this.amount = amount;
            this.baseUnit = baseUnit;
        }
    }
}