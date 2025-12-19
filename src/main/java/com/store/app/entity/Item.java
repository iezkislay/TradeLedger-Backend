package com.store.app.entity;

import com.store.app.enums.BaseUnit;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "items")
@Getter
@Setter
public class Item {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "item_code", unique = true)
    private String itemCode;

    private String name;
    private String brand;
    private String category;

    @Enumerated(EnumType.STRING)
    private BaseUnit baseUnit;

    private BigDecimal sellingPrice;
    private BigDecimal costPrice;
    private BigDecimal minStock;

    private LocalDateTime createdAt = LocalDateTime.now();
}

