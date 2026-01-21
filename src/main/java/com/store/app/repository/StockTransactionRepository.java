package com.store.app.repository;

import com.store.app.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.UUID;

public interface StockTransactionRepository
        extends JpaRepository<StockTransaction, UUID> {

    boolean existsByItem_Id(UUID itemId);

    @Query("""
        SELECT MAX(st.createdAt)
        FROM StockTransaction st
        WHERE
          st.transactionType = com.store.app.enums.StockTxnType.OUT
          AND st.referenceType = com.store.app.enums.ReferenceType.BILL
          AND st.referenceId = :billId
          AND st.item.id = :itemId
    """)
    LocalDateTime findLastFulfilledAt(UUID billId, UUID itemId);
}
