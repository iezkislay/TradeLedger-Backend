package com.store.app.repository;

import com.store.app.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockTransactionRepository
        extends JpaRepository<StockTransaction, UUID> {

    boolean existsByItem_Id(UUID itemId);
}
