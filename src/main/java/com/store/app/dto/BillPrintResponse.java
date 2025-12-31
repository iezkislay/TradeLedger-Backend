package com.store.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Immutable DTO for bill printing / invoice rendering
 * Used for PDF / thermal / WhatsApp / UI print preview
 */
public record BillPrintResponse(

        // =====================
        // Bill Meta
        // =====================
        String billNumber,
        String billCode,
        LocalDateTime createdAt,
        String paymentType,

        // =====================
        // Customer (nullable for walk-in)
        // =====================
        String customerName,
        String customerMobile,
        String customerAddress,

        // =====================
        // Line Items
        // =====================
        List<Item> items,

        // =====================
        // Totals
        // =====================
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal dueAmount

) {

    /**
     * Single bill line item for print
     */
    public record Item(
            String name,
            BigDecimal quantity,
            String unit,
            BigDecimal price,
            BigDecimal amount
    ) {}
}
