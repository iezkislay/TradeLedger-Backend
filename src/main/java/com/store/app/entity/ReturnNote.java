package com.store.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "return_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnNote {

    @Id
    @GeneratedValue
    private UUID id;

    /* =====================================================
       🔐 OPTIMISTIC LOCKING (RACE CONDITION GUARD)
       ===================================================== */
    @Version
    private Long version;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    /**
     * Sum of returned items value (before discount proration)
     */
    @Column(nullable = false)
    private BigDecimal returnedGrossAmount = BigDecimal.ZERO;

    /**
     * Discount clawed back proportionally
     */
    @Column(nullable = false)
    private BigDecimal clawedDiscountAmount = BigDecimal.ZERO;

    /**
     * returnedGrossAmount - clawedDiscountAmount
     */
    @Column(nullable = false)
    private BigDecimal netReturnAmount = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /* =====================================================
       🔒 FINALIZATION STATE (BUSINESS SOURCE OF TRUTH)
       ===================================================== */

    /**
     * Whether this return note has been finalized
     * (ledger + refund effects applied)
     */
    @Column(nullable = false)
    private boolean finalized = false;

    /**
     * Timestamp when finalizeReturn() was executed
     * Used for audit & reconciliation
     */
    @Column
    private LocalDateTime finalizedAt;
}
