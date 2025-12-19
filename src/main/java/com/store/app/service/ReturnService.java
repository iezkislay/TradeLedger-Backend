package com.store.app.service;

import com.store.app.entity.*;
import com.store.app.enums.PaymentType;
import com.store.app.enums.ReferenceType;
import com.store.app.enums.StockTxnType;
import com.store.app.repository.CustomerLedgerRepository;
import com.store.app.repository.CustomerRepository;
import com.store.app.repository.StockRepository;
import com.store.app.repository.StockTransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ReturnService {

    private final StockRepository stockRepo;
    private final StockTransactionRepository stockTxnRepo;
    private final CustomerRepository customerRepo;
    private final CustomerLedgerRepository ledgerRepo;
    private final AuthService authService;
    private final AuditService auditService;

    public ReturnService(
            StockRepository stockRepo,
            StockTransactionRepository stockTxnRepo,
            CustomerRepository customerRepo,
            CustomerLedgerRepository ledgerRepo,
            AuthService authService,
            AuditService auditService
    ) {
        this.stockRepo = stockRepo;
        this.stockTxnRepo = stockTxnRepo;
        this.customerRepo = customerRepo;
        this.ledgerRepo = ledgerRepo;
        this.authService = authService;
        this.auditService = auditService;
    }

    @Transactional
    public void returnItem(BillItem billItem, BigDecimal quantity, User user) {

        authService.requireBillingOrOwner(user);

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Return quantity must be greater than zero");
        }

        if (quantity.compareTo(billItem.getQuantity()) > 0) {
            throw new RuntimeException("Return quantity exceeds sold quantity");
        }

        Stock stock = stockRepo.findById(billItem.getItem().getId())
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        stock.setQuantity(stock.getQuantity().add(quantity));
        stockRepo.save(stock);

        StockTransaction txn = new StockTransaction();
        txn.setItem(billItem.getItem());
        txn.setTransactionType(StockTxnType.IN);
        txn.setQuantity(quantity);
        txn.setReferenceType(ReferenceType.BILL);
        txn.setReferenceId(billItem.getBill().getId());

        stockTxnRepo.save(txn);

        Bill bill = billItem.getBill();

        auditService.log(
                "BILL",
                bill.getId(),
                "RETURN",
                null,
                "Returned qty: " + quantity,
                user
        );

        if (bill.getPaymentType() == PaymentType.CREDIT) {

            Customer customer = bill.getCustomer();
            BigDecimal refundAmount =
                    billItem.getPrice().multiply(quantity);

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
