package com.store.app.entity;

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
    private Bill bill;

    @ManyToOne(optional = false)
    private Item item;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private BigDecimal amount;

    // 🆕 Phase-2B: Fulfilment tracking
    @Column(nullable = false)
    private BigDecimal fulfilledQty = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal pendingQty = BigDecimal.ZERO;

    @Column(nullable = false)
    private String fulfilmentStatus;
    // FULL | PARTIAL | PENDING
}
