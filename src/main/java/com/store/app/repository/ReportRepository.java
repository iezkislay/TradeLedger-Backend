package com.store.app.repository;

import com.store.app.dto.*;
import com.store.app.entity.Bill;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReportRepository extends Repository<Bill, UUID> {

    // =========================
    // 🟢 EXISTING REPORTS (KEEP)
    // =========================

    @Query(value = """
        SELECT
            DATE(created_at) AS date,
            COUNT(*) AS totalBills,
            SUM(total_amount) AS totalSales
        FROM bills
        WHERE DATE(created_at) = :date
        GROUP BY DATE(created_at)
        """, nativeQuery = true)
    List<DailySalesReportRow> dailySales(
            @Param("date") LocalDate date
    );

    @Query(value = """
        SELECT
            i.id AS itemId,
            i.name AS itemName,
            s.quantity AS quantity
        FROM stock s
        JOIN items i ON i.id = s.item_id
        ORDER BY i.name
        """, nativeQuery = true)
    List<StockSummaryRow> stockSummary();

    @Query(value = """
        SELECT
            i.id AS itemId,
            i.name AS itemName,
            SUM(bi.quantity) AS quantitySold,
            SUM(bi.amount) AS totalAmount
        FROM bill_items bi
        JOIN items i ON i.id = bi.item_id
        JOIN bills b ON b.id = bi.bill_id
        WHERE DATE(b.created_at) BETWEEN :from AND :to
        GROUP BY i.id, i.name
        ORDER BY totalAmount DESC
        """, nativeQuery = true)
    List<ItemSalesRow> itemSales(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // =========================
    // 🆕 PHASE-2 REPORTS (FIXED)
    // =========================

    // 1️⃣ Item-wise sales (fulfilled qty)
    @Query(value = """
        SELECT
            i.name,
            i.category,
            SUM(bi.fulfilled_qty),
            SUM(bi.fulfilled_qty * bi.price)
        FROM bill_items bi
        JOIN items i ON bi.item_id = i.id
        JOIN bills b ON bi.bill_id = b.id
        WHERE DATE(b.created_at) BETWEEN :from AND :to
        GROUP BY i.name, i.category
        ORDER BY 4 DESC
        """, nativeQuery = true)
    List<Object[]> itemWiseSalesRaw(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // 2️⃣ Category-wise sales
    @Query(value = """
        SELECT
            i.category,
            SUM(bi.fulfilled_qty),
            SUM(bi.fulfilled_qty * bi.price)
        FROM bill_items bi
        JOIN items i ON bi.item_id = i.id
        JOIN bills b ON bi.bill_id = b.id
        WHERE DATE(b.created_at) BETWEEN :from AND :to
        GROUP BY i.category
        ORDER BY 3 DESC
        """, nativeQuery = true)
    List<Object[]> categoryWiseSalesRaw(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // 3️⃣ Daily sales range
    @Query(value = """
        SELECT
            DATE(b.created_at),
            SUM(b.total_amount)
        FROM bills b
        WHERE DATE(b.created_at) BETWEEN :from AND :to
        GROUP BY DATE(b.created_at)
        ORDER BY 1
        """, nativeQuery = true)
    List<Object[]> dailySalesRangeRaw(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
