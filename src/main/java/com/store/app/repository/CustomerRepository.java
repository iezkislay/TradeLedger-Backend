package com.store.app.repository;

import com.store.app.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /* =====================================================
       🔍 CUSTOMER SEARCH (SAFE)
       ===================================================== */

    @Query("""
        SELECT c
        FROM Customer c
        WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
           OR c.mobile LIKE CONCAT('%', :q, '%')
           OR c.customerCode LIKE CONCAT('%', :q, '%')
        ORDER BY c.name
    """)
    List<Customer> search(@Param("q") String q, Pageable pageable);
}
