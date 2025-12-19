package com.store.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "returns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Return {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Bill bill;

    @ManyToOne
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    private String reason;

    private LocalDateTime createdAt = LocalDateTime.now();
}
