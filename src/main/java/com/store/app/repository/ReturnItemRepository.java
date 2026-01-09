package com.store.app.repository;

import com.store.app.entity.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ReturnItemRepository extends JpaRepository<ReturnItem, UUID> {

    /* =====================================================
       🧾 RETURN ITEMS — BY RETURN NOTE
       ===================================================== */
    List<ReturnItem> findByReturnNote_Id(UUID returnNoteId);

    /* =====================================================
       🔥 TRUE RETURN VALUE — DELIVERED + PENDING (₹)
       ===================================================== */
    @Query("""
        SELECT COALESCE(SUM(
            (ri.returnedDeliveredQty + ri.returnedPendingQty) * ri.price
        ), 0)
        FROM ReturnItem ri
        WHERE ri.returnNote.bill.id = :billId
    """)
    BigDecimal sumReturnValueByBill(UUID billId);

    /* =====================================================
       🔍 SPLIT — DELIVERED vs PENDING (₹)
       ===================================================== */
    @Query("""
        SELECT
            COALESCE(SUM(ri.returnedDeliveredQty * ri.price), 0),
            COALESCE(SUM(ri.returnedPendingQty * ri.price), 0)
        FROM ReturnItem ri
        WHERE ri.returnNote.bill.id = :billId
    """)
    Object[] returnValueSplit(UUID billId);

    /* =====================================================
       🧾 SUMMARY PER BILL (UI / REPORTING)
       ===================================================== */
    @Query("""
        SELECT
            i.name,
            i.baseUnit,
            (ri.returnedDeliveredQty + ri.returnedPendingQty),
            ((ri.returnedDeliveredQty + ri.returnedPendingQty) * ri.price)
        FROM ReturnItem ri
        JOIN ri.item i
        WHERE ri.returnNote.bill.id = :billId
    """)
    List<Object[]> findReturnSummaryByBill(UUID billId);

    /* =====================================================
       🔢 TOTAL RETURNED QTY — COMBINED (LEGACY / SAFETY)
       ===================================================== */
    @Query("""
        SELECT COALESCE(
            SUM(ri.returnedDeliveredQty + ri.returnedPendingQty), 0
        )
        FROM ReturnItem ri
        WHERE ri.billItem.id = :billItemId
    """)
    BigDecimal getTotalReturnedQtyForBillItem(UUID billItemId);

    /* =====================================================
       ✅ TOTAL RETURNED QTY — DELIVERED ONLY
       ===================================================== */
    @Query("""
        SELECT COALESCE(SUM(ri.returnedDeliveredQty), 0)
        FROM ReturnItem ri
        WHERE ri.billItem.id = :billItemId
    """)
    BigDecimal getTotalReturnedDeliveredQtyForBillItem(UUID billItemId);

    /* =====================================================
       ✅ TOTAL RETURNED QTY — PENDING ONLY
       ===================================================== */
    @Query("""
        SELECT COALESCE(SUM(ri.returnedPendingQty), 0)
        FROM ReturnItem ri
        WHERE ri.billItem.id = :billItemId
    """)
    BigDecimal getTotalReturnedPendingQtyForBillItem(UUID billItemId);
}
