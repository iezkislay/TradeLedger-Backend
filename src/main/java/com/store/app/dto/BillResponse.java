package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class BillResponse {

    private UUID billId;
    private String billNumber;
    private String billCode;
    private LocalDateTime billDate;
    private String paymentType;

    private String customerName;
    private String customerMobile;
    private String customerAddress;
    private String customerCode;

    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    private BigDecimal returnedAmount;
    private BigDecimal effectiveTotal;

    private BigDecimal amountPaid;
    private BigDecimal dueAmount;

    // ✅ NEW — exposed state (read-only)
    private String state;
}
