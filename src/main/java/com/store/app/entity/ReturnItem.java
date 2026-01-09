package com.store.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "return_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItem {

    @Id
    @GeneratedValue
    private UUID id;

    /* =====================
       📄 RETURN NOTE (FINANCIAL EVENT)
       ===================== */
    @ManyToOne(optional = false)
    @JoinColumn(name = "return_note_id", nullable = false)
    private ReturnNote returnNote;

    /* =====================
       🧾 BILL CONTEXT
       ===================== */
    @ManyToOne(optional = false)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    /* =====================
       🧾 BILL ITEM CONTEXT
       ===================== */
    @ManyToOne(optional = false)
    @JoinColumn(name = "bill_item_id", nullable = false)
    private BillItem billItem;

    /* =====================
       📦 ITEM (DENORMALIZED FOR REPORTING)
       ===================== */
    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /* ======================
       🔒 ERP TRUTH FIELDS
       ====================== */

    /**
     * Quantity returned from DELIVERED stock
     * → increases inventory
     */
    @Column(name = "returned_delivered_qty", nullable = false)
    private BigDecimal returnedDeliveredQty = BigDecimal.ZERO;

    /**
     * Quantity returned from PENDING (short-sold)
     * → does NOT affect inventory
     */
    @Column(name = "returned_pending_qty", nullable = false)
    private BigDecimal returnedPendingQty = BigDecimal.ZERO;

    /**
     * Unit price snapshot from BillItem.price
     * (immutable, audit-safe)
     */
    @Column(nullable = false)
    private BigDecimal price;

    /* ======================
       🧮 DERIVED HELPERS (NON-PERSISTED)
       ====================== */

    @Transient
    public BigDecimal getTotalReturnedQty() {
        return returnedDeliveredQty.add(returnedPendingQty);
    }

    @Transient
    public BigDecimal getTotalAmount() {
        return getTotalReturnedQty().multiply(price);
    }

    /* ======================
       🛡️ DEFENSIVE VALIDATION
       ====================== */
    @PrePersist
    @PreUpdate
    private void validateQuantities() {
        if (returnedDeliveredQty == null || returnedPendingQty == null) {
            throw new IllegalStateException("Returned quantities cannot be null");
        }
        if (returnedDeliveredQty.signum() < 0 || returnedPendingQty.signum() < 0) {
            throw new IllegalStateException("Returned quantities cannot be negative");
        }
        if (getTotalReturnedQty().signum() <= 0) {
            throw new IllegalStateException("Total returned quantity must be positive");
        }
    }

    /* ======================
       ⏱️ AUDIT
       ====================== */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
