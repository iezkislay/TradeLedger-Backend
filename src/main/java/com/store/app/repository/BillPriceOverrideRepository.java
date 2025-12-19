package com.store.app.repository;

import com.store.app.entity.BillPriceOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BillPriceOverrideRepository extends JpaRepository<BillPriceOverride, UUID> {
}
