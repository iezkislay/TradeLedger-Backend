package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * =====================================================
 * CREATE ITEM REQUEST (SAFE ADDITION)
 * Purpose:
 * - Accept item creation + opening stock in one API
 * - Does NOT replace Item entity
 * - Keeps all existing item/stock APIs intact
 * Used by:
 * POST /api/items
 * Notes:
 * - openingStock is optional
 * - Validation remains in service layer
 * =====================================================
 */
@Getter
@Setter
public class CreateItemRequest {

    // ===== ITEM FIELDS =====
    private String name;
    private String brand;
    private String category;
    private String baseUnit;

    private BigDecimal costPrice;
    private BigDecimal sellingPrice;

    private BigDecimal minStock;

    // ===== STOCK FIELD (NEW) =====
    private BigDecimal openingStock;
}
