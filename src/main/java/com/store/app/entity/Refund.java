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

    /* =====================
       🔗 LINK TO BILL (LEGACY)
       ===================== */
    @ManyToOne
    private Bill bill;

    /* =====================
       🔗 LINK TO RETURN (STEP B)
       ===================== */
    @ManyToOne
    @JoinColumn(name = "return_id")
    private Return returnEntity;

    @ManyToOne
    private Customer customer;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private RefundMode refundMode;

    private String reason;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "return_note_id")
    private ReturnNote returnNote;

    private LocalDateTime createdAt = LocalDateTime.now();
}
