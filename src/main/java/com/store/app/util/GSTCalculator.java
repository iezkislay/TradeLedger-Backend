package com.store.app.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class GSTCalculator {

    public static GstBreakdown calculateInclusive(BigDecimal price, BigDecimal gstRate) {

        BigDecimal divisor = BigDecimal.ONE.add(
                gstRate.divide(BigDecimal.valueOf(100))
        );

        BigDecimal taxable = price.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal gst = price.subtract(taxable);

        BigDecimal cgst = gst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        BigDecimal sgst = cgst;

        return new GstBreakdown(taxable, cgst, sgst);
    }

    public record GstBreakdown(
            BigDecimal taxable,
            BigDecimal cgst,
            BigDecimal sgst
    ) {}
}