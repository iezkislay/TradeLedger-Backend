package com.store.app.repository;

import com.store.app.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    // ✅ Existing (UNCHANGED)
    long countByCategory(String category);

    // =========================
    // 🔍 SEARCH (UI AUTOCOMPLETE)
    // =========================
    @Query("""
        SELECT i
        FROM Item i
        WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(i.itemCode) LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY i.name
    """)
    List<Item> search(
            @Param("q") String q,
            Pageable pageable
    );
}
