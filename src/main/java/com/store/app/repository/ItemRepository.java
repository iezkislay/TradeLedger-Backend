package com.store.app.repository;

import com.store.app.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    // =====================================================
    // EXISTING — DO NOT TOUCH
    // =====================================================
    long countByCategory(String category);

    // =====================================================
    // EXISTING — DO NOT TOUCH (Billing autocomplete)
    // =====================================================
    @Query("""
        SELECT i
        FROM Item i
        WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(i.itemCode) LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY i.name
    """)
    java.util.List<Item> search(String q, Pageable pageable);

    /* =====================================================
       🆕 PAGED SEARCH (FIXED — POSTGRES SAFE)
       ===================================================== */

    @Query("""
        SELECT i
        FROM Item i
        WHERE (
              :q IS NULL
              OR LOWER(i.name) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
              OR LOWER(i.itemCode) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
              OR LOWER(i.brand) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
        )
        AND (:category IS NULL OR i.category = :category)
    """)
    Page<Item> searchPaged(
            String q,
            String category,
            Pageable pageable
    );
}
