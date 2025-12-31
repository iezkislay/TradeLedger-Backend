package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class BillItemRequest {

    private UUID itemId;
    private BigDecimal quantity;
    private BigDecimal price;

}
