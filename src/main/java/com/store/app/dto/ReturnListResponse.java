package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ReturnListResponse {

    private UUID returnId;
    private LocalDate returnDate;
    private String billCode;
    private String returnType;
    private List<Item> items;

    @Getter
    @Setter
    public static class Item {
        private String itemName;
        private String baseUnit;
        private String reason;
        private Number quantity;
    }
}
