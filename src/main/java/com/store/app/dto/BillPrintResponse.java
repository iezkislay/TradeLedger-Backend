package com.store.app.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class BillPrintResponse {

    private final String billNumber;
    private final String billCode;
    private final LocalDateTime billDate;
    private final String paymentType;

    private final String customerName;
    private final String customerMobile;
    private final String customerAddress;

    private final List<Item> items;

    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal total;
    private final BigDecimal paid;
    private final BigDecimal due;

    // ✅ NEW — display only
    private final String state;

    public BillPrintResponse(
            String billNumber,
            String billCode,
            LocalDateTime billDate,
            String paymentType,
            String customerName,
            String customerMobile,
            String customerAddress,
            List<Item> items,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal total,
            BigDecimal paid,
            BigDecimal due,
            String state
    ) {
        this.billNumber = billNumber;
        this.billCode = billCode;
        this.billDate = billDate;
        this.paymentType = paymentType;
        this.customerName = customerName;
        this.customerMobile = customerMobile;
        this.customerAddress = customerAddress;
        this.items = items;
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
        this.paid = paid;
        this.due = due;
        this.state = state;
    }

    @Getter
    public static class Item {
        private final String name;
        private final BigDecimal quantity;
        private final String unit;
        private final BigDecimal price;
        private final BigDecimal amount;

        public Item(
                String name,
                BigDecimal quantity,
                String unit,
                BigDecimal price,
                BigDecimal amount
        ) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
            this.price = price;
            this.amount = amount;
        }
    }
}
