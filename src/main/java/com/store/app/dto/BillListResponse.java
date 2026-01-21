package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class BillListResponse {

    private UUID billId;
    private String billNumber;
    private String billCode;
    private LocalDateTime billDate;
    private String paymentType;

    private String customerName;
    private String customerMobile;

    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;

    // ✅ NEW — exposed state
    private String state;
}
