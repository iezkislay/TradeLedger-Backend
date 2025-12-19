package com.store.app.repository;

import com.store.app.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BillItemRepository extends JpaRepository<BillItem, UUID> {

    List<BillItem> findByPendingQtyGreaterThan(BigDecimal qty);
}
