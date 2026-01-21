package com.store.app.repository;

import com.store.app.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    BigDecimal sumRefundedAmountForBill(@Param("billId") UUID billId);

    /* =====================================================
       🆕 ALIAS — REQUIRED BY NEW REFUND FLOW (SAFE ADDITION)
       ===================================================== */

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Refund r
        WHERE r.bill.id = :billId
    """)
    BigDecimal sumRefundedAmountByBillId(@Param("billId") UUID billId);

    /* =====================================================
       🆕 REQUIRED HELPER — SIGNATURE VARIANT (ADD ONLY)
       ===================================================== */

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Refund r
        WHERE r.bill.id = :billId
    """)
    BigDecimal sumRefundedAmountByBill(UUID billId);

    /* =====================================================
       🆕 TOTAL REFUNDED — EXPLICIT HELPER (ADD ONLY)
       ===================================================== */

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Refund r
        WHERE r.bill.id = :billId
    """)
    BigDecimal getTotalRefundedByBill(@Param("billId") UUID billId);

    /* =====================================================
       DASHBOARD — PHASE 3B (REFUND KPI)
       ===================================================== */

    // 💸 Total refunds issued today (all modes)
    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Refund r
        WHERE DATE(r.createdAt) = :date
    """)
    BigDecimal todayRefunds(@Param("date") LocalDate date);

    @Query("""
        SELECT r
        FROM Refund r
        WHERE r.bill.id = :billId
        ORDER BY r.createdAt ASC
    """)
    List<Refund> findAllByBillId(UUID billId);

    @Query("""
    SELECT COALESCE(SUM(r.amount), 0)
    FROM Refund r
    WHERE r.returnNote.id = :returnNoteId
""")
    BigDecimal sumRefundedAmountByReturnNote(UUID returnNoteId);

    @Query("""
    SELECT COALESCE(SUM(r.amount), 0)
    FROM Refund r
    WHERE r.returnNote.id = :returnNoteId
""")
    BigDecimal sumRefundedAmountByReturnNoteId(UUID returnNoteId);

}
