package com.store.app.repository;

import com.store.app.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    // Customers having outstanding balance (credit customers)
    List<Customer> findByBalanceGreaterThanOrderByBalanceDesc(BigDecimal balance);
}
