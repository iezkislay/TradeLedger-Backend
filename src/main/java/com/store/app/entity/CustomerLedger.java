package com.store.app.entity;

import com.store.app.enums.LedgerType;
import com.store.app.enums.ReferenceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_ledger")
@Getter
@Setter
public class CustomerLedger {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "bill_id")
    private Bill bill;

    /**
     * DEBIT  → Bill raised
     * CREDIT → Payment received / refund / return credit
     * ADJUSTMENT → Waiver / rounding / discount
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private LedgerType entryType;

    /**
     * SOURCE OF LEDGER ENTRY
     * BILL / PAYMENT / RETURN / REFUND / ADJUSTMENT
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false)
    private ReferenceType referenceType;

    /**
     * Always POSITIVE
     * Meaning decided by entryType
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
