package com.store.app.repository;

import com.store.app.entity.CustomerCounter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerCounterRepository
        extends JpaRepository<CustomerCounter, Integer> {
}
