package com.store.app.repository;

import com.store.app.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {

    /* =====================================================
       EXISTING — OPERATIONAL QUERIES (FIXED JOIN)
       ===================================================== */

    // 🟡 Low stock items (used in reports / alerts)
    @Query("""
        SELECT s
        FROM Stock s
        JOIN s.item i
        WHERE s.quantity < i.minStock
    """)
    List<Stock> findLowStockItems();

    /* =====================================================
       DASHBOARD — PHASE 3B (INVENTORY KPIs)
       ===================================================== */

    // 📉 Count of low stock items
    @Query("""
        SELECT COUNT(s)
        FROM Stock s
        JOIN s.item i
        WHERE s.quantity < i.minStock
    """)
    Long lowStockCount();

    // 💰 Total stock value at cost price (inventory valuation)
    @Query("""
        SELECT COALESCE(SUM(s.quantity * i.costPrice), 0)
        FROM Stock s
        JOIN s.item i
    """)
    BigDecimal totalStockValue();
}
