package com.store.app.repository;

import com.store.app.entity.Bill;
import com.store.app.entity.BillItem;
import com.store.app.enums.PaymentType;
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
       DASHBOARD — PHASE 3B (READ-ONLY, SAFE ADDITIONS)
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

    // 💳 Payment split (raw)
    @Query("""
        SELECT b.paymentType, COALESCE(SUM(b.totalAmount), 0)
        FROM Bill b
        WHERE DATE(b.createdAt) = :date
        GROUP BY b.paymentType
    """)
    List<Object[]> paymentSplitRaw(LocalDate date);

    // 💳 Payment split (clean Map for DTO)
    default Map<String, BigDecimal> paymentSplit(LocalDate date) {
        return paymentSplitRaw(date)
                .stream()
                .collect(Collectors.toMap(
                        r -> r[0].toString(),
                        r -> (BigDecimal) r[1]
                ));
    }

    // 🚚 Pending fulfilment value (money already collected)
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
}
