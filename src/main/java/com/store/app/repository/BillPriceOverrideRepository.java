package com.store.app.repository;

import com.store.app.entity.BillPriceOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BillPriceOverrideRepository
        extends JpaRepository<BillPriceOverride, UUID> {

    Optional<BillPriceOverride> findByBill_Id(UUID billId);
}
