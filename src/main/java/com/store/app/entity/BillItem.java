package com.store.app.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "bill_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bill_id")
    @JsonBackReference
    private Bill bill;

    @ManyToOne(optional = false)
    private Item item;

    /**
     * ORIGINAL quantity sold (never changes)
     * quantity = fulfilledQty + pendingQty + returnedQty
     */
    @Column(nullable = false)
    private BigDecimal quantity;

    /**
     * Price per unit at time of billing (GST inclusive)
     */
    @Column(nullable = false)
    private BigDecimal price;

    /**
     * Total amount = quantity × price (IMMUTABLE)
     */
    @Column(nullable = false)
    private BigDecimal amount;

    // ================= GST FIELDS =================

    @Column(name = "hsn_code")
    private String hsnCode;

    @Column(name = "gst_rate")
    private BigDecimal gstRate;

    @Column(name = "taxable_amount")
    private BigDecimal taxableAmount = BigDecimal.ZERO;

    @Column(name = "cgst_amount")
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(name = "sgst_amount")
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    // ================= FULFILMENT =================

    /**
     * Quantity returned by customer
     */
    @Column(name = "returned_quantity", nullable = false)
    private BigDecimal returnedQty = BigDecimal.ZERO;

    /**
     * Quantity physically delivered to customer
     */
    @Column(nullable = false)
    private BigDecimal fulfilledQty = BigDecimal.ZERO;

    /**
     * Quantity not yet delivered (short-sell / pending)
     */
    @Column(nullable = false)
    private BigDecimal pendingQty = BigDecimal.ZERO;

    /**
     * FULL | PARTIAL | PENDING | RETURNED
     */
    @Column(nullable = false)
    private String fulfilmentStatus;

    /* =========================
       DERIVED (NOT STORED)
       ========================= */

    /**
     * Net quantity after returns
     * netQuantity = quantity − returnedQty
     */
    @Transient
    public BigDecimal getNetQuantity() {
        return quantity.subtract(returnedQty);
    }
}