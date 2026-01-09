package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * =====================================================
 * CUSTOMER BILL RESPONSE (READ-ONLY)
 * Used for:
 * GET /api/customers/{id}/bills
 * Design principles:
 * - No item details (list-safe)
 * - Ledger-derived paid / due
 * - UI-ready
 * - No mutation risk
 * =====================================================
 */
@Getter
@Setter
public class CustomerBillResponse {

    private UUID billId;

    private String billNumber;
    private String billCode;

    private LocalDateTime billDate;

    private String paymentType;

    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;
}
