package com.store.app.entity;

import com.store.app.enums.ReferenceType;
import com.store.app.enums.StockTxnType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockTransaction {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Item item;

    @Enumerated(EnumType.STRING)
    private StockTxnType transactionType;

    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    private ReferenceType referenceType;

    private UUID referenceId;

    private LocalDateTime createdAt = LocalDateTime.now();
}
