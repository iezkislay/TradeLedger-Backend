package com.store.app.repository;

import com.store.app.dto.StockSummaryDto;
import com.store.app.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {

    /* =====================================================
       EXISTING — OPERATIONAL QUERIES (UNCHANGED)
       ===================================================== */

    // 🟡 Low stock items (ENTITY — used internally, NOT for controller JSON)
    @Query("""
        SELECT s
        FROM Stock s
        JOIN s.item i
        WHERE s.quantity < i.minStock
    """)
    List<Stock> findLowStockItems();

    /* =====================================================
       🟢 SAFE DTO QUERY — LOW STOCK (CONTROLLER USE)
       ===================================================== */

    /**
     * Projection-based query to avoid LazyInitializationException.
     * Used ONLY for API response mapping.
     */
    @Query("""
        SELECT
            i.id,
            i.name,
            s.quantity,
            i.minStock
        FROM Stock s
        JOIN s.item i
        WHERE s.quantity < i.minStock
    """)
    List<Object[]> findLowStockRaw();

    /* =====================================================
       🆕 STOCK SUMMARY — SAFE DTO (READ-ONLY API)
       ===================================================== */

    /**
     * Stock summary for dashboard / reports.
     * Uses DTO projection to avoid Hibernate proxy issues.
     *
     * ⚠️ IMPORTANT:
     * Constructor order MUST match StockSummaryDto exactly.
     */
    @Query("""
        SELECT new com.store.app.dto.StockSummaryDto(
            i.id,
            i.name,
            i.itemCode,
            i.brand,
            i.category,
            i.baseUnit,
            s.quantity,
            i.minStock,
            i.costPrice
        )
        FROM Stock s
        JOIN s.item i
        ORDER BY i.category, i.name
    """)
    List<StockSummaryDto> fetchStockSummary();

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

    /* =====================================================
       STOCK LOOKUP — READ ONLY (SAFE ADDITION)
       ===================================================== */

    // 📦 Available quantity for an item (UI hint only)
    @Query("""
        SELECT COALESCE(s.quantity, 0)
        FROM Stock s
        WHERE s.item.id = :itemId
    """)
    BigDecimal findAvailableQty(@Param("itemId") UUID itemId);
}
