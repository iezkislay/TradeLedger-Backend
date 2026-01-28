package com.store.app.repository;

import com.store.app.entity.Bill;
import com.store.app.enums.BillState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public interface BillRepository extends JpaRepository<Bill, UUID> {

    /* =====================================================
       EXISTING — DO NOT TOUCH (CORE BILLING FEATURES)
       ===================================================== */

    // 🔢 Used for daily bill number generation
    long countByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // 🔢 Used for daily ESTIMATE number generation (Phase 3.5)
    long countByStateAndCreatedAtBetween(
            BillState state,
            LocalDateTime start,
            LocalDateTime end
    );

    // 💳 Used in reports & analytics (range-based)
    @Query("""
        SELECT b.paymentType, COALESCE(SUM(b.totalAmount), 0)
        FROM Bill b
        WHERE b.createdAt BETWEEN :start AND :end
        GROUP BY b.paymentType
    """)
    List<Object[]> sumByPaymentType(
            LocalDateTime start,
            LocalDateTime end
    );

    // 💰 Used in reports
    @Query("""
        SELECT COALESCE(SUM(b.totalAmount), 0)
        FROM Bill b
        WHERE b.createdAt BETWEEN :start AND :end
    """)
    BigDecimal totalAmountBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    /* =====================================================
       DASHBOARD — PHASE 3B (EXISTING)
       ===================================================== */

    // 🔥 Today sales
    @Query("""
        SELECT COALESCE(SUM(b.totalAmount), 0)
        FROM Bill b
        WHERE DATE(b.createdAt) = :date
    """)
    BigDecimal todaySales(LocalDate date);

    // 📅 Month sales
    @Query("""
        SELECT COALESCE(SUM(b.totalAmount), 0)
        FROM Bill b
        WHERE YEAR(b.createdAt) = :year
          AND MONTH(b.createdAt) = :month
    """)
    BigDecimal monthSales(int year, int month);

    // 📊 Average bill value (today)
    @Query("""
        SELECT COALESCE(AVG(b.totalAmount), 0)
        FROM Bill b
        WHERE DATE(b.createdAt) = :date
    """)
    BigDecimal avgBillValue(LocalDate date);

    // 💳 Payment split (single day – raw)
    @Query("""
        SELECT b.paymentType, COALESCE(SUM(b.totalAmount), 0)
        FROM Bill b
        WHERE DATE(b.createdAt) = :date
        GROUP BY b.paymentType
    """)
    List<Object[]> paymentSplitRaw(LocalDate date);

    // 💳 Payment split (single day – clean Map)
    default Map<String, BigDecimal> paymentSplit(LocalDate date) {
        return paymentSplitRaw(date)
                .stream()
                .collect(Collectors.toMap(
                        r -> r[0].toString(),
                        r -> (BigDecimal) r[1]
                ));
    }

    // 🚚 Pending fulfilment value
    @Query("""
        SELECT COALESCE(SUM(bi.pendingQty * bi.price), 0)
        FROM BillItem bi
    """)
    BigDecimal totalPendingValue();

    // 🚚 Pending fulfilment quantity
    @Query("""
        SELECT COALESCE(SUM(bi.pendingQty), 0)
        FROM BillItem bi
        WHERE bi.pendingQty > 0
    """)
    Long totalPendingItems();

    /* =====================================================
       DASHBOARD — RANGE-BASED (NEW, SAFE ADDITIONS)
       ===================================================== */

    // 💳 Payment split (date range)
    @Query("""
        SELECT b.paymentType, COALESCE(SUM(b.totalAmount), 0)
        FROM Bill b
        WHERE DATE(b.createdAt) BETWEEN :from AND :to
        GROUP BY b.paymentType
    """)
    List<Object[]> paymentSplit(LocalDate from, LocalDate to);

    // 📈 Sales trend (date → sales)
    @Query("""
        SELECT DATE(b.createdAt), COALESCE(SUM(b.totalAmount), 0)
        FROM Bill b
        WHERE DATE(b.createdAt) BETWEEN :from AND :to
        GROUP BY DATE(b.createdAt)
        ORDER BY DATE(b.createdAt)
    """)
    List<Object[]> salesTrend(LocalDate from, LocalDate to);

    // 📊 Average bill value (range)
    @Query("""
        SELECT COALESCE(AVG(b.totalAmount), 0)
        FROM Bill b
        WHERE DATE(b.createdAt) BETWEEN :from AND :to
    """)
    BigDecimal avgBillValue(LocalDate from, LocalDate to);

    /* =====================================================
       READ — LIST / SEARCH BILLS (EXISTING)
       ===================================================== */

    @Query(
            value = """
        SELECT *
        FROM bills b
        WHERE (
            :search IS NULL
            OR b.bill_number ILIKE CONCAT('%', :search, '%')
            OR b.bill_code   ILIKE CONCAT('%', :search, '%')
        )
        ORDER BY b.created_at DESC
    """,
            nativeQuery = true
    )
    List<Bill> searchBills(String search);

    /* =====================================================
       READ — CUSTOMER BILLS (NEW, SAFE ADDITION)
       ===================================================== */

    List<Bill> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
