package com.store.app.repository;

import com.store.app.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface RefundRepository extends JpaRepository<Refund, UUID> {

    /* =====================================================
       EXISTING — BILL-LEVEL REFUND CALCULATION (DO NOT TOUCH)
       ===================================================== */

    // 🔁 Used to validate refunds against a specific bill
    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Refund r
        WHERE r.bill.id = :billId
    """)
    BigDecimal sumRefundedAmountForBill(UUID billId);

    /* =====================================================
       DASHBOARD — PHASE 3B (REFUND KPI)
       ===================================================== */

    // 💸 Total refunds issued today (all modes)
    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Refund r
        WHERE DATE(r.createdAt) = :date
    """)
    BigDecimal todayRefunds(LocalDate date);
}
