package com.store.app.repository;

import com.store.app.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {

    // 🟡 Low stock items (quantity < minStock)
    @Query("""
        SELECT s
        FROM Stock s
        JOIN Item i ON i.id = s.id
        WHERE s.quantity < i.minStock
    """)
    List<Stock> findLowStockItems();
}
