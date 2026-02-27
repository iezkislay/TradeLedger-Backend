package com.store.app.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ReturnPrintResponse {

    private final String returnNumber;
    private final LocalDateTime returnDate;

    private final String customerName;
    private final String customerMobile;
    private final String customerAddress;

    private final List<Item> items;

    private final BigDecimal returnedGross;
    private final BigDecimal discountClawedBack;
    private final BigDecimal netReturn;

    private final BigDecimal projectedDue;

    public ReturnPrintResponse(
            String returnNumber,
            LocalDateTime returnDate,
            String customerName,
            String customerMobile,
            String customerAddress,
            List<Item> items,
            BigDecimal returnedGross,
            BigDecimal discountClawedBack,
            BigDecimal netReturn,
            BigDecimal projectedDue
    ) {
        this.returnNumber = returnNumber;
        this.returnDate = returnDate;
        this.customerName = customerName;
        this.customerMobile = customerMobile;
        this.customerAddress = customerAddress;
        this.items = items;
        this.returnedGross = returnedGross;
        this.discountClawedBack = discountClawedBack;
        this.netReturn = netReturn;
        this.projectedDue = projectedDue;
    }

    @Getter
    public static class Item {

        private final String name;
        private final BigDecimal quantity;
        private final String unit;
        private final BigDecimal rate;
        private final BigDecimal amount;

        public Item(
                String name,
                BigDecimal quantity,
                String unit,
                BigDecimal rate,
                BigDecimal amount
        ) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
            this.rate = rate;
            this.amount = amount;
        }
    }
}
