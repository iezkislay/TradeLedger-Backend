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
    @JoinColumn(name = "bill_item_id")
    private BillItem billItem;

    private BigDecimal originalPrice;
    private BigDecimal overriddenPrice;

    @ManyToOne
    @JoinColumn(name = "overridden_by")
    private User overriddenBy;

    private LocalDateTime overriddenAt = LocalDateTime.now();
}
