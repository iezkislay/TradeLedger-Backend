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

    /**
     * Total quantity sold (financial commitment)
     * quantity = fulfilledQty + pendingQty
     */
    @Column(nullable = false)
    private BigDecimal quantity;

    /**
     * Price per unit at time of billing
     */
    @Column(nullable = false)
    private BigDecimal price;

    /**
     * Total amount = quantity × price
     */
    @Column(nullable = false)
    private BigDecimal amount;

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
     * FULL | PARTIAL | PENDING
     */
    @Column(nullable = false)
    private String fulfilmentStatus;
}
