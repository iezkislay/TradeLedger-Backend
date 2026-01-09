package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class BillResponse {

    private UUID billId;
    private String billNumber;
    private String billCode;
    private LocalDateTime billDate;

    private String paymentType;

    // Customer
    private String customerName;
    private String customerMobile;
    private String customerAddress;
    private String customerCode;

    // Original amounts (immutable)
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    // 🆕 Returns-aware
    private BigDecimal returnedAmount;
    private BigDecimal effectiveTotal;

    // Settlement
    private BigDecimal amountPaid;
    private BigDecimal dueAmount;

    // Line items
    private List<BillItemResponse> items;
}
