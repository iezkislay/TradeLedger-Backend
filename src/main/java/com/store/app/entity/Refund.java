package com.store.app.entity;

import com.store.app.enums.RefundMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Refund {

    @Id
    @GeneratedValue
    private UUID id;

    // 🔗 Bill against which refund is done
    @ManyToOne(optional = false)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    // 👤 Customer (can be null for walk-in CASH refunds)
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // 💰 Refund amount
    @Column(nullable = false)
    private BigDecimal amount;

    // 💳 CASH / UPI / CREDIT
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundMode refundMode;

    // 📝 Reason for refund
    private String reason;

    // 🔐 Who processed the refund
    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // ⏰ Timestamp
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
