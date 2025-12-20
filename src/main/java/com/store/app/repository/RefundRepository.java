package com.store.app.repository;

import com.store.app.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.UUID;

public interface RefundRepository extends JpaRepository<Refund, UUID> {

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Refund r
        WHERE r.bill.id = :billId
    """)
    BigDecimal sumRefundedAmountForBill(UUID billId);
}
