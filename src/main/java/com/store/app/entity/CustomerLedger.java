package com.store.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_ledger")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerLedger {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Customer customer;

    @ManyToOne
    private Bill bill;

    private BigDecimal debit = BigDecimal.ZERO;
    private BigDecimal credit = BigDecimal.ZERO;

    private LocalDateTime createdAt = LocalDateTime.now();
}
