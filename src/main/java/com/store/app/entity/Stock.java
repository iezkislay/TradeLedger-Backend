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

    @Id
    @Column(name = "item_id")
    private UUID itemId;

    @Column(nullable = false)
    private BigDecimal quantity = BigDecimal.ZERO;
}
