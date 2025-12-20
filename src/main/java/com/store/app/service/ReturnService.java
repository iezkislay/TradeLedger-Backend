package com.store.app.service;

import com.store.app.dto.PartialReturnRequest;
import com.store.app.entity.*;
import com.store.app.enums.PaymentType;
import com.store.app.enums.ReferenceType;
import com.store.app.enums.ReturnType;
import com.store.app.enums.StockTxnType;
import com.store.app.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ReturnService {

    private final BillItemRepository billItemRepo;
    private final StockRepository stockRepo;
    private final StockTransactionRepository stockTxnRepo;
    private final CustomerRepository customerRepo;
    private final CustomerLedgerRepository ledgerRepo;
    private final AuthService authService;
    private final AuditService auditService;

    public ReturnService(
            BillItemRepository billItemRepo,
            StockRepository stockRepo,
            StockTransactionRepository stockTxnRepo,
            CustomerRepository customerRepo,
            CustomerLedgerRepository ledgerRepo,
            AuthService authService,
            AuditService auditService
    ) {
        this.billItemRepo = billItemRepo;
        this.stockRepo = stockRepo;
        this.stockTxnRepo = stockTxnRepo;
        this.customerRepo = customerRepo;
        this.ledgerRepo = ledgerRepo;
        this.authService = authService;
        this.auditService = auditService;
    }

    /**
     * 🔁 Partial Return (DELIVERED or PENDING)
     */
    @Transactional
    public void partialReturn(PartialReturnRequest req, User user) {

        // 🔒 RBAC
        authService.requireBillingOrOwner(user);

        BillItem bi = billItemRepo.findById(req.getBillItemId())
                .orElseThrow(() -> new RuntimeException("Bill item not found"));

        BigDecimal qty = req.getQuantity();

        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Return quantity must be greater than zero");
        }

        Bill bill = bi.getBill();

        // =========================
        // RETURN FROM DELIVERED
        // =========================
        if (req.getReturnType() == ReturnType.DELIVERED) {

            if (qty.compareTo(bi.getFulfilledQty()) > 0) {
                throw new RuntimeException("Return exceeds delivered quantity");
            }

            // 📦 Stock IN
            Stock stock = stockRepo.findById(bi.getItem().getId())
                    .orElseThrow(() -> new RuntimeException("Stock not found"));

            stock.setQuantity(stock.getQuantity().add(qty));
            stockRepo.save(stock);

            // 📊 Stock transaction
            StockTransaction txn = new StockTransaction();
            txn.setItem(bi.getItem());
            txn.setTransactionType(StockTxnType.IN);
            txn.setQuantity(qty);
            txn.setReferenceType(ReferenceType.RETURN);
            txn.setReferenceId(bill.getId());
            stockTxnRepo.save(txn);

            // Update delivered qty
            bi.setFulfilledQty(bi.getFulfilledQty().subtract(qty));
        }

        // =========================
        // CANCEL FROM PENDING
        // =========================
        else if (req.getReturnType() == ReturnType.PENDING) {

            if (qty.compareTo(bi.getPendingQty()) > 0) {
                throw new RuntimeException("Return exceeds pending quantity");
            }

            // No stock movement
            bi.setPendingQty(bi.getPendingQty().subtract(qty));
        }

        else {
            throw new RuntimeException("Invalid return type");
        }

        // =========================
        // COMMON UPDATES
        // =========================
        bi.setQuantity(bi.getQuantity().subtract(qty));

        // Update fulfilment status
        if (bi.getPendingQty().compareTo(BigDecimal.ZERO) == 0 &&
                bi.getFulfilledQty().compareTo(BigDecimal.ZERO) > 0) {
            bi.setFulfilmentStatus("FULL");
        } else if (bi.getFulfilledQty().compareTo(BigDecimal.ZERO) == 0) {
            bi.setFulfilmentStatus("PENDING");
        } else {
            bi.setFulfilmentStatus("PARTIAL");
        }

        billItemRepo.save(bi);

        // =========================
        // AUDIT LOG
        // =========================
        auditService.log(
                "BILL",
                bill.getId(),
                "PARTIAL_RETURN",
                null,
                "Type=" + req.getReturnType() + ", Qty=" + qty +
                        ", Reason=" + req.getReason(),
                user
        );

        // =========================
        // LEDGER (CREDIT ONLY)
        // =========================
        if (bill.getPaymentType() == PaymentType.CREDIT &&
                bill.getCustomer() != null) {

            Customer customer = bill.getCustomer();
            BigDecimal refundAmount = bi.getPrice().multiply(qty);

            customer.setBalance(
                    customer.getBalance().subtract(refundAmount)
            );
            customerRepo.save(customer);

            CustomerLedger ledger = new CustomerLedger();
            ledger.setCustomer(customer);
            ledger.setBill(bill);
            ledger.setCredit(refundAmount);

            ledgerRepo.save(ledger);
        }
    }
}
