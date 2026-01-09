package com.store.app.repository;

import com.store.app.entity.ReturnNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ReturnNoteRepository extends JpaRepository<ReturnNote, UUID> {

    /* =====================================================
       EXISTING — DO NOT TOUCH
       ===================================================== */

    List<ReturnNote> findByBill_Id(UUID billId);

    /* =====================================================
       🆕 FINALIZED NET RETURN (EXCLUDING CURRENT NOTE)
       ===================================================== */

    @Query("""
        SELECT COALESCE(SUM(rn.netReturnAmount), 0)
        FROM ReturnNote rn
        WHERE rn.bill.id = :billId
          AND rn.finalized = true
          AND rn.id <> :currentReturnNoteId
    """)
    BigDecimal sumFinalizedNetReturnByBillExcludingCurrent(
            UUID billId,
            UUID currentReturnNoteId
    );

    @Query("""
    SELECT COALESCE(SUM(rn.netReturnAmount), 0)
    FROM ReturnNote rn
    WHERE rn.bill.id = :billId
      AND rn.finalized = true
""")
    BigDecimal sumFinalizedNetReturnByBill(UUID billId);



    @Query("""
    SELECT COALESCE(SUM(rn.netReturnAmount), 0)
    FROM ReturnNote rn
    WHERE rn.bill.id = :billId
      AND rn.finalized = true
""")
    BigDecimal sumFinalizedReturnByBill(UUID billId);


}
