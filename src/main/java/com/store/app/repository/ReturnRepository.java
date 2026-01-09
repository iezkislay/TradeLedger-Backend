package com.store.app.repository;

import com.store.app.entity.Return;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReturnRepository extends JpaRepository<Return, UUID> {

    /* =========================
       READ — LIST RETURNS
       ========================= */

    List<Return> findByBill_IdOrderByCreatedAtDesc(UUID billId);

    List<Return> findByCustomer_IdOrderByCreatedAtDesc(UUID customerId);
}
