package com.store.app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
public class Customer {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "customer_code", unique = true, nullable = false)
    private String customerCode;

    /**
     * ❌ DEPRECATED
     * Ledger is the ONLY source of truth.
     * This field is kept only to avoid breaking older code paths.
     */
    @Deprecated
    @Transient   // 🔥 THIS IS THE FIX
    private BigDecimal balance;

    private String name;
    private String mobile;

    // Optional address
    private String address;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "gstin", length = 15)
    private String gstin;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
