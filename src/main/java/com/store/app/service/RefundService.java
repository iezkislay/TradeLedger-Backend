package com.store.app.service;

import com.store.app.dto.RefundRequest;
import com.store.app.entity.*;
import com.store.app.enums.RefundMode;
import com.store.app.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RefundService {

    private final RefundRepository refundRepo;
    private final BillRepository billRepo;
    private final CustomerRepository customerRepo;
    private final CustomerLedgerRepository ledgerRepo;
    private final AuthService authService;
    private final AuditService auditService;

    public RefundService(
            RefundRepository refundRepo,
            BillRepository billRepo,
            CustomerRepository customerRepo,
            CustomerLedgerRepository ledgerRepo,
            AuthService authService,
            AuditService auditService
    ) {
        this.refundRepo = refundRepo;
        this.billRepo = billRepo;
        this.customerRepo = customerRepo;
        this.ledgerRepo = ledgerRepo;
        this.authService = authService;
        this.auditService = auditService;
    }

    @Transactional
    public void processRefund(RefundRequest req, User user) {

        // 🔒 OWNER ONLY
        authService.requireOwner(user);

        Bill bill = billRepo.findById(req.getBillId())
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        BigDecimal amount = req.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Refund amount must be positive");
        }

        // 🔐 Prevent over-refund
        BigDecimal alreadyRefunded =
                refundRepo.sumRefundedAmountForBill(bill.getId());

        BigDecimal refundable =
                bill.getTotalAmount().subtract(alreadyRefunded);

        if (amount.compareTo(refundable) > 0) {
            throw new RuntimeException("Refund exceeds refundable amount");
        }

        // 1️⃣ Save refund record
        Refund refund = new Refund();
        refund.setBill(bill);
        refund.setCustomer(bill.getCustomer());
        refund.setAmount(amount);
        refund.setRefundMode(req.getRefundMode());
        refund.setReason(req.getReason());
        refund.setCreatedBy(user);

        refundRepo.save(refund);

        // 2️⃣ CREDIT refund → ledger + balance update
        if (req.getRefundMode() == RefundMode.CREDIT) {

            Customer customer = bill.getCustomer();

            if (customer == null) {
                throw new RuntimeException("CREDIT refund requires customer");
            }

            // Update customer balance
            customer.setBalance(
                    customer.getBalance().subtract(amount)
            );
            customerRepo.save(customer);

            // Ledger entry
            CustomerLedger ledger = new CustomerLedger();
            ledger.setCustomer(customer);
            ledger.setBill(bill);
            ledger.setCredit(amount);

            ledgerRepo.save(ledger);
        }

        // 3️⃣ Audit log
        auditService.log(
                "BILL",
                bill.getId(),
                "REFUND",
                null,
                "Refund ₹" + amount + " via " + req.getRefundMode(),
                user
        );
    }
}
