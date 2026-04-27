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
@NoArgsConstructor
@AllArgsConstructor
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

    // ================= GST FIELDS =================

    @Column(name = "gst_rate")
    private BigDecimal gstRate;

    @Column(name = "hsn_code")
    private String hsnCode;

    // ================= META =================

    private LocalDateTime createdAt = LocalDateTime.now();
}