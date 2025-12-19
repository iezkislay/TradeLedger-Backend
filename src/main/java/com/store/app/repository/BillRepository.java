package com.store.app.repository;

import com.store.app.entity.Bill;
import com.store.app.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {

    // 🔢 Count bills within a date range (used for daily billCode)
    long countByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // 💳 Sum of bill amounts grouped by payment type (CASH / CREDIT / UPI etc.)
    @Query("""
        SELECT b.paymentType, COALESCE(SUM(b.totalAmount), 0)
        FROM Bill b
        WHERE b.createdAt BETWEEN :start AND :end
        GROUP BY b.paymentType
    """)
    List<Object[]> sumByPaymentType(
            LocalDateTime start,
            LocalDateTime end
    );

    // 💰 Total sales amount within a date range
    @Query("""
        SELECT COALESCE(SUM(b.totalAmount), 0)
        FROM Bill b
        WHERE b.createdAt BETWEEN :start AND :end
    """)
    BigDecimal totalAmountBetween(
            LocalDateTime start,
            LocalDateTime end
    );
}
