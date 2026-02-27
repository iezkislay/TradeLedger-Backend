package com.store.app.dto;

import com.store.app.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class CreateBillRequest {

    /* =========================
       CORE BILL FIELDS
       ========================= */

    /**
     * Payment mode of the bill
     * CASH | UPI | CREDIT
     */
    private PaymentType paymentType;

    /**
     * Existing customer reference
     * Nullable for WALK-IN bills
     */
    private UUID customerId;

    /**
     * Used when customerId is null
     * (walk-in or auto-created customer)
     */
    private String customerName;
    private String customerMobile;

    /**
     * 🆕 Optional customer address
     */
    private String customerAddress;

    /**
     * Bill line items
     */
    private List<BillItemRequest> items;

    /* =========================
       BILLING REALISM (PHASE-3)
       ========================= */

    /**
     * Flat discount applied on bill (₹)
     * Nullable, must be >= 0
     */
    private BigDecimal discountAmount;

    /**
     * Amount actually received from customer (₹)
     * Nullable, must be >= 0
     */
    private BigDecimal amountPaid;

    private UUID workOrderId;

}
