package com.store.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bill_price_overrides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillPriceOverride {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bill_id", referencedColumnName = "id")
    private Bill bill;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal originalAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal overriddenAmount;

    @Column
    private String reason;

    @ManyToOne(optional = false)
    @JoinColumn(name = "overridden_by", referencedColumnName = "id")
    private User overriddenBy;

    @Column(nullable = false)
    private LocalDateTime overriddenAt;
}
