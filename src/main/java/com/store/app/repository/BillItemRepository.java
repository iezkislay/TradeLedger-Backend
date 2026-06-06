package com.store.app.repository;

import com.store.app.dto.PendingFulfilmentRow;
import com.store.app.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BillItemRepository extends JpaRepository<BillItem, UUID> {

    List<BillItem> findByBillId(UUID billId);

    // Creation order = insertion order (via ID)
    List<BillItem> findByBillIdOrderByLineNumberAsc(UUID billId);

    /**
     * Pending fulfilments =
     * quantity − fulfilled − (returnedDelivered + returnedPending) > 0
     */
    @Query("""
        SELECT bi
        FROM BillItem bi
        WHERE
          bi.quantity
          - bi.fulfilledQty
          - COALESCE((
              SELECT SUM(
                  COALESCE(ri.returnedDeliveredQty, 0)
                + COALESCE(ri.returnedPendingQty, 0)
              )
              FROM ReturnItem ri
              WHERE ri.billItem.id = bi.id
          ), 0) > 0
    """)
    List<BillItem> findPendingFulfilments();

    @Query("""
        SELECT bi.item.name, SUM(bi.quantity)
        FROM BillItem bi
        JOIN bi.bill b
        WHERE DATE(b.createdAt) BETWEEN :from AND :to
        GROUP BY bi.item.name
        ORDER BY SUM(bi.quantity) DESC
    """)
    List<Object[]> topItems(LocalDate from, LocalDate to);

    @Query("""
    SELECT new com.store.app.dto.PendingFulfilmentRow(
        bi.id,
        b.id,
        b.billCode,
        c.name,
        i.itemCode,
        i.name,
        bi.fulfilledQty,
        (bi.quantity
         - bi.fulfilledQty
         - COALESCE((
             SELECT SUM(
                 COALESCE(ri.returnedDeliveredQty, 0)
               + COALESCE(ri.returnedPendingQty, 0)
             )
             FROM ReturnItem ri
             WHERE ri.billItem.id = bi.id
         ), 0)
        ),
        bi.fulfilmentStatus,
        MAX(tx.createdAt)
    )
    FROM BillItem bi
    JOIN bi.bill b
    LEFT JOIN b.customer c
    JOIN bi.item i
    LEFT JOIN StockTransaction tx
           ON tx.referenceId = b.id
          AND tx.transactionType = com.store.app.enums.StockTxnType.OUT
    WHERE b.state = com.store.app.enums.BillState.ACTIVE
      AND (
          bi.quantity
          - bi.fulfilledQty
          - COALESCE((
              SELECT SUM(
                  COALESCE(ri.returnedDeliveredQty, 0)
                + COALESCE(ri.returnedPendingQty, 0)
              )
              FROM ReturnItem ri
              WHERE ri.billItem.id = bi.id
          ), 0)
      ) > 0
    GROUP BY
        bi.id, b.id, b.billCode, c.name,
        i.itemCode, i.name,
        bi.fulfilledQty, bi.quantity, bi.fulfilmentStatus
    ORDER BY b.createdAt DESC
""")
    List<PendingFulfilmentRow> findPendingFulfilmentsGrouped();

}
