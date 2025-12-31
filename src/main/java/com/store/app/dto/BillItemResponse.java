package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class BillItemResponse {

    private String itemCode;
    private String itemName;
    private String baseUnit;

    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal amount;

    private BigDecimal fulfilledQty;
    private BigDecimal pendingQty;
    private String fulfilmentStatus;

    /* =========================
       GETTERS & SETTERS
       ========================= */

}
