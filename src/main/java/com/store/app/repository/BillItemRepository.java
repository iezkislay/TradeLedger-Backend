package com.store.app.repository;

import com.store.app.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BillItemRepository extends JpaRepository<BillItem, UUID> {

    List<BillItem> findByBillId(UUID billId);

    // Creation order = insertion order (via ID)
    List<BillItem> findByBillIdOrderByIdAsc(UUID billId);

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
}
