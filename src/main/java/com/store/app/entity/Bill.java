package com.store.app.entity;

import com.store.app.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    // Human-friendly / date-based code
    // Example: BILL-20251218-01
    @Column(name = "bill_code", unique = true)
    private String billCode;

    // Sequential bill number
    // Example: BILL-0001
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

    // ✅ IMPORTANT: Required for Invoice PDF
    @OneToMany(mappedBy = "bill", fetch = FetchType.EAGER)
    private List<BillItem> items;
}
