package com.store.app.repository;

import com.store.app.entity.CustomerLedger;
import com.store.app.dto.CustomerBalanceView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CustomerLedgerRepository
        extends JpaRepository<CustomerLedger, UUID> {

    /* =====================================================
       CUSTOMER STATEMENT / HISTORY
       ===================================================== */

    List<CustomerLedger> findByCustomer_IdOrderByCreatedAtAsc(UUID customerId);

    /* =====================================================
       BILL SETTLEMENT — SINGLE SOURCE OF TRUTH
       ===================================================== */

    @Query("""
    SELECT COALESCE(SUM(
        CASE
            WHEN l.entryType = com.store.app.enums.LedgerType.DEBIT THEN l.amount
            WHEN l.entryType IN (
                com.store.app.enums.LedgerType.CREDIT,
                com.store.app.enums.LedgerType.RETURN_CREDIT,
                com.store.app.enums.LedgerType.ADJUSTMENT
            ) THEN -l.amount
            ELSE 0
        END
    ), 0)
    FROM CustomerLedger l
    WHERE l.bill.id = :billId
""")
    BigDecimal getDueForBill(@Param("billId") UUID billId);

    /* =====================================================
       🆕 CURRENT LEGALLY RECOVERABLE DUE (ADD ONLY)
       ===================================================== */

    @Query("""
        SELECT
            COALESCE(
                SUM(CASE
                        WHEN l.entryType = com.store.app.enums.LedgerType.DEBIT
                        THEN l.amount
                        ELSE 0
                END), 0
            )
            -
            COALESCE(
                SUM(CASE
                        WHEN l.entryType = com.store.app.enums.LedgerType.CREDIT
                        THEN l.amount
                        ELSE 0
                END), 0
            )
            -
            COALESCE(
                SUM(CASE
                        WHEN l.entryType = com.store.app.enums.LedgerType.RETURN_CREDIT
                        THEN l.amount
                        ELSE 0
                END), 0
            )
        FROM CustomerLedger l
        WHERE l.bill.id = :billId
    """)
    BigDecimal getCurrentDue(@Param("billId") UUID billId);

    /* =====================================================
       🆕 PURE CASH PAID — BILL LEVEL (ADD ONLY)
       ===================================================== */

    @Query("""
        SELECT COALESCE(SUM(l.amount), 0)
        FROM CustomerLedger l
        WHERE l.bill.id = :billId
          AND l.entryType = com.store.app.enums.LedgerType.CREDIT
    """)
    BigDecimal getTotalPaidForBill(@Param("billId") UUID billId);

    /* =====================================================
       PENDING BILLS
       ===================================================== */

    @Query("""
        SELECT
            b.id,
            b.billCode,
            b.createdAt,
            b.totalAmount,
            COALESCE(SUM(
                CASE
                    WHEN l.entryType IN (
                        com.store.app.enums.LedgerType.CREDIT,
                        com.store.app.enums.LedgerType.RETURN_CREDIT,
                        com.store.app.enums.LedgerType.ADJUSTMENT
                    ) THEN l.amount
                    ELSE 0
                END
            ), 0)
        FROM Bill b
        LEFT JOIN CustomerLedger l ON l.bill.id = b.id
        WHERE b.customer.id = :customerId
        GROUP BY b.id, b.billCode, b.createdAt, b.totalAmount
        HAVING
            b.totalAmount -
            COALESCE(SUM(
                CASE
                    WHEN l.entryType IN (
                        com.store.app.enums.LedgerType.CREDIT,
                        com.store.app.enums.LedgerType.RETURN_CREDIT,
                        com.store.app.enums.LedgerType.ADJUSTMENT
                    ) THEN l.amount
                    ELSE 0
                END
            ), 0) > 0
    """)
    List<Object[]> findPendingBills(@Param("customerId") UUID customerId);

    /* =====================================================
       KPI — LEDGER TRUTH
       ===================================================== */

    // 💵 Cash actually collected
    @Query("""
        SELECT COALESCE(SUM(l.amount), 0)
        FROM CustomerLedger l
        WHERE l.entryType = 'CREDIT'
    """)
    BigDecimal totalCashReceived();

    // 🧾 Waived / adjusted amount
    @Query("""
        SELECT COALESCE(SUM(l.amount), 0)
        FROM CustomerLedger l
        WHERE l.entryType = 'ADJUSTMENT'
    """)
    BigDecimal totalWaived();

    // 💳 Outstanding across all customers
    @Query("""
        SELECT COALESCE(SUM(
            CASE
                WHEN l.entryType = 'DEBIT' THEN l.amount
                WHEN l.entryType IN ('CREDIT', 'RETURN_CREDIT', 'ADJUSTMENT') THEN -l.amount
                ELSE 0
            END
        ), 0)
        FROM CustomerLedger l
    """)
    BigDecimal totalOutstandingLedger();

    /* =====================================================
       LEDGER BALANCE — CUSTOMER LEVEL (SAFE, USED BY API)
       ===================================================== */

    @Query("""
        SELECT COALESCE(SUM(
            CASE
                WHEN l.entryType = com.store.app.enums.LedgerType.DEBIT THEN l.amount
                WHEN l.entryType = com.store.app.enums.LedgerType.CREDIT THEN -l.amount
                WHEN l.entryType = com.store.app.enums.LedgerType.RETURN_CREDIT THEN -l.amount
                WHEN l.entryType = com.store.app.enums.LedgerType.ADJUSTMENT THEN -l.amount
                ELSE 0
            END
        ), 0)
        FROM CustomerLedger l
        WHERE l.customer.id = :customerId
    """)
    BigDecimal calculateBalance(@Param("customerId") UUID customerId);

    /* =====================================================
       CUSTOMER STATEMENT — TIMELINE (DESC)
       ===================================================== */

    @Query("""
        SELECT l
        FROM CustomerLedger l
        WHERE l.customer.id = :customerId
        ORDER BY l.createdAt DESC
    """)
    List<CustomerLedger> findStatement(@Param("customerId") UUID customerId);

    /* =====================================================
       CUSTOMER BALANCE VIEW — DASHBOARD / PAGINATION (LEGACY)
       ===================================================== */

    @Query("""
        SELECT new com.store.app.dto.CustomerBalanceView(
            c.id,
            c.customerCode,
            c.name,
            c.mobile,
            c.address,
            COALESCE(
                SUM(
                    CASE
                        WHEN l.entryType = 'CREDIT' THEN l.amount
                        WHEN l.entryType = 'DEBIT' THEN -l.amount
                        WHEN l.entryType = 'RETURN_CREDIT' THEN -l.amount
                        WHEN l.entryType = 'ADJUSTMENT' THEN -l.amount
                        ELSE 0
                    END
                ), 0
            )
        )
        FROM Customer c
        LEFT JOIN CustomerLedger l ON l.customer = c
        GROUP BY c.id, c.customerCode, c.name, c.mobile
        ORDER BY c.createdAt DESC
    """)
    Page<CustomerBalanceView> fetchCustomerBalances(Pageable pageable);

    /* =====================================================
       🆕 AGGREGATION — CUSTOMER BALANCES (FAST, SAFE)
       ===================================================== */

    @Query("""
        SELECT
            cl.customer.id,
            COALESCE(SUM(
                CASE
                    WHEN cl.entryType = 'DEBIT' THEN cl.amount
                    WHEN cl.entryType = 'CREDIT' THEN -cl.amount
                    WHEN cl.entryType = 'RETURN_CREDIT' THEN -cl.amount
                    WHEN cl.entryType = 'ADJUST' THEN -cl.amount
                    ELSE 0
                END
            ), 0)
        FROM CustomerLedger cl
        GROUP BY cl.customer.id
    """)
    List<Object[]> calculateBalances();

    /* =====================================================
       🆕 AGGREGATION — LAST TRANSACTION DATE
       ===================================================== */

    @Query("""
        SELECT cl.customer.id, MAX(cl.createdAt)
        FROM CustomerLedger cl
        GROUP BY cl.customer.id
    """)
    List<Object[]> findLastTxnDates();
}
