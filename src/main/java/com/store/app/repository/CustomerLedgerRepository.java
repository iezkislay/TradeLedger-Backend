package com.store.app.repository;

import com.store.app.entity.CustomerLedger;
import com.store.app.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CustomerLedgerRepository
        extends JpaRepository<CustomerLedger, UUID> {

    /* =====================================================
       EXISTING — CUSTOMER STATEMENT / HISTORY (DO NOT TOUCH)
       ===================================================== */

    // 📒 Fetch full ledger for a customer (chronological)
    List<CustomerLedger> findByCustomer_IdOrderByCreatedAtAsc(UUID customerId);

    /* =====================================================
       DASHBOARD — PHASE 3B (CREDIT KPI)
       ===================================================== */

    // 💳 Total outstanding credit across all customers
    @Query("""
        SELECT COALESCE(SUM(c.balance), 0)
        FROM Customer c
        WHERE c.balance > 0
    """)
    BigDecimal totalOutstanding();
}
