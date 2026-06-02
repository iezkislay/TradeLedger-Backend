package com.store.app.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class CustomerBillPrintResponse {

    // =====================================================
    // BILL HEADER
    // =====================================================

    private final UUID billId;

    private final String billNumber;
    private final String billCode;

    private final LocalDateTime billDate;

    private final String paymentType;
    private final String state;

    // =====================================================
    // CUSTOMER
    // =====================================================

    private final String customerName;
    private final String customerMobile;
    private final String customerAddress;

    // =====================================================
    // ITEMS
    // =====================================================

    private final List<Item> items;

    // =====================================================
    // RETURN SUMMARY
    // =====================================================

    private final BigDecimal returnedAmount;
    private final BigDecimal refundedAmount;

    // =====================================================
    // FINANCIAL SUMMARY
    // =====================================================

    private final BigDecimal subtotal;
    private final BigDecimal discount;

    private final BigDecimal effectiveTotal;

    private final BigDecimal paidAmount;

    private final BigDecimal adjustment;

    private final BigDecimal dueAmount;

    public CustomerBillPrintResponse(
            UUID billId,
            String billNumber,
            String billCode,
            LocalDateTime billDate,
            String paymentType,
            String state,
            String customerName,
            String customerMobile,
            String customerAddress,
            List<Item> items,
            BigDecimal returnedAmount,
            BigDecimal refundedAmount,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal effectiveTotal,
            BigDecimal paidAmount,
            BigDecimal adjustment,
            BigDecimal dueAmount
    ) {
        this.billId = billId;
        this.billNumber = billNumber;
        this.billCode = billCode;
        this.billDate = billDate;
        this.paymentType = paymentType;
        this.state = state;
        this.customerName = customerName;
        this.customerMobile = customerMobile;
        this.customerAddress = customerAddress;
        this.items = items;
        this.returnedAmount = returnedAmount;
        this.refundedAmount = refundedAmount;
        this.subtotal = subtotal;
        this.discount = discount;
        this.effectiveTotal = effectiveTotal;
        this.paidAmount = paidAmount;
        this.adjustment = adjustment;
        this.dueAmount = dueAmount;
    }

    // =====================================================
    // ITEM
    // =====================================================

    @Getter
    public static class Item {

        private final String itemCode;
        private final String itemName;
        private final String brand;

        private final BigDecimal orderedQty;
        private final BigDecimal returnedQty;
        private final BigDecimal netQty;

        private final BigDecimal rate;
        private final BigDecimal amount;

        private final String status;
        private final String unit;

        public Item(
                String itemCode,
                String itemName,
                String brand,
                String unit,
                BigDecimal orderedQty,
                BigDecimal returnedQty,
                BigDecimal netQty,
                BigDecimal rate,
                BigDecimal amount,
                String status
        ) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.brand = brand;
            this.unit = unit;
            this.orderedQty = orderedQty;
            this.returnedQty = returnedQty;
            this.netQty = netQty;
            this.rate = rate;
            this.amount = amount;
            this.status = status;
        }
    }
}