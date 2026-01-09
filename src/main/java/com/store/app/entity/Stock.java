package com.store.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    /**
     * item_id is BOTH:
     * - PK of stock
     * - FK to item
     */
    @Id
    @Column(name = "item_id")
    private UUID itemId;

    /**
     * 🔥 CRITICAL FIX
     * Shared primary key mapping
     */
    @MapsId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(nullable = false)
    private BigDecimal quantity = BigDecimal.ZERO;
}
