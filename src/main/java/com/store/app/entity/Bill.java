package com.store.app.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.store.app.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bill {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "bill_code", unique = true)
    private String billCode;

    @Column(name = "bill_number", nullable = false, unique = true)
    private String billNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

    @ManyToOne
    private Customer customer;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ✅ FIX 1: initialize list
    // ✅ FIX 2: prevent infinite JSON loop
    @OneToMany(mappedBy = "bill", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<BillItem> items = new ArrayList<>();
}
