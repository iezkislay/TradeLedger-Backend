package com.store.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ItemSearchResponse {

    private UUID id;

    private String itemCode;
    private String name;
    private String brand;
    private String category;

    private String baseUnit;

    private BigDecimal sellingPrice;

    /**
     * OWNER only — null for others
     */
    private BigDecimal costPrice;

    /**
     * Minimum stock threshold (for alerts / UI)
     */
    private BigDecimal minStock;

    private BigDecimal availableStock;
}
