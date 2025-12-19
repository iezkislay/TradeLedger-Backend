package com.store.app.repository;

import com.store.app.entity.CustomerLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerLedgerRepository
        extends JpaRepository<CustomerLedger, UUID> {

    // Fetch ledger entries for a customer in chronological order
    List<CustomerLedger> findByCustomer_IdOrderByCreatedAtAsc(UUID customerId);
}
