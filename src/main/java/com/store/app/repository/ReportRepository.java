package com.store.app.repository;

import com.store.app.dto.*;
import com.store.app.entity.Bill;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReportRepository extends Repository<Bill, UUID> {

    // =========================
    // 🟢 EXISTING REPORTS (KEEP)
    // =========================

    // Daily sales (single date)
    @Query(value = """
        SELECT
            DATE(created_at) AS date,
            COUNT(*) AS totalBills,
            SUM(total_amount) AS totalSales
        FROM bills
        WHERE DATE(created_at) = :date
        GROUP BY DATE(created_at)
        """, nativeQuery = true)
    List<DailySalesReportRow> dailySales(LocalDate date);

    // Stock summary
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

    // Item sales (OLD – based on requested quantity)
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
    List<ItemSalesRow> itemSales(LocalDate from, LocalDate to);


    // =========================
    // 🆕 NEW PHASE-2 REPORTS
    // =========================

    // 1️⃣ Item-wise sales (based on fulfilled quantity)
    @Query(value = """
        SELECT 
            i.name AS itemName,
            i.category AS category,
            SUM(bi.fulfilled_qty) AS quantitySold,
            SUM(bi.fulfilled_qty * bi.price) AS totalAmount
        FROM bill_items bi
        JOIN items i ON bi.item_id = i.id
        JOIN bills b ON bi.bill_id = b.id
        WHERE b.created_at BETWEEN :from AND :to
        GROUP BY i.name, i.category
        ORDER BY totalAmount DESC
        """, nativeQuery = true)
    List<ItemSalesReportDto> itemWiseSales(
            LocalDate from,
            LocalDate to
    );

    // 2️⃣ Category-wise sales
    @Query(value = """
        SELECT 
            i.category AS category,
            SUM(bi.fulfilled_qty) AS quantitySold,
            SUM(bi.fulfilled_qty * bi.price) AS totalAmount
        FROM bill_items bi
        JOIN items i ON bi.item_id = i.id
        JOIN bills b ON bi.bill_id = b.id
        WHERE b.created_at BETWEEN :from AND :to
        GROUP BY i.category
        ORDER BY totalAmount DESC
        """, nativeQuery = true)
    List<CategorySalesReportDto> categoryWiseSales(
            LocalDate from,
            LocalDate to
    );

    // 3️⃣ Daily sales (date range, NEW version)
    @Query(value = """
        SELECT 
            DATE(b.created_at) AS date,
            SUM(b.total_amount) AS totalSales
        FROM bills b
        WHERE DATE(b.created_at) BETWEEN :from AND :to
        GROUP BY DATE(b.created_at)
        ORDER BY date
        """, nativeQuery = true)
    List<DailySalesReportDto> dailySales(
            LocalDate from,
            LocalDate to
    );
}
