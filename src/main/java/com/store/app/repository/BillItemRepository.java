package com.store.app.repository;

import com.store.app.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BillItemRepository extends JpaRepository<BillItem, UUID> {

    /**
     * Existing — used for pending fulfilment checks
     */
    List<BillItem> findByPendingQtyGreaterThan(BigDecimal qty);

    /**
     * ===============================
     * DASHBOARD ANALYTICS (ADD ONLY)
     * ===============================
     * Top selling items by quantity (date range)
     */
    @Query("""
        SELECT bi.item.name, SUM(bi.quantity)
        FROM BillItem bi
        JOIN bi.bill b
        WHERE DATE(b.createdAt) BETWEEN :from AND :to
        GROUP BY bi.item.name
        ORDER BY SUM(bi.quantity) DESC
    """)
    List<Object[]> topItems(LocalDate from, LocalDate to);
}
