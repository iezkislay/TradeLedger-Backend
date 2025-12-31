package com.store.app.service;

import com.store.app.dto.CustomerBalanceView;
import com.store.app.dto.CustomerListResponse;
import com.store.app.dto.CustomerStatementRowDto;
import com.store.app.dto.PendingBillResponse;
import com.store.app.entity.Customer;
import com.store.app.entity.CustomerLedger;
import com.store.app.entity.User;
import com.store.app.enums.LedgerType;
import com.store.app.repository.CustomerLedgerRepository;
import com.store.app.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepo;
    private final CustomerLedgerRepository ledgerRepo;
    private final AuthService authService;

    public CustomerService(
            CustomerRepository customerRepo,
            CustomerLedgerRepository ledgerRepo,
            AuthService authService
    ) {
        this.customerRepo = customerRepo;
        this.ledgerRepo = ledgerRepo;
        this.authService = authService;
    }

    // =====================================================
    // ✅ CREATE CUSTOMER (RBAC)
    // =====================================================
    public Customer createCustomer(Customer customer, User user) {

        authService.requireBillingOrOwner(user);

        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new RuntimeException("Customer name is required");
        }

        if (customer.getMobile() == null || customer.getMobile().isBlank()) {
            throw new RuntimeException("Customer mobile is required");
        }

        customer.setCustomerCode(generateCustomerCode());
        return customerRepo.save(customer);
    }

    // =====================================================
    // ✅ LIGHTWEIGHT CREATE (AUTO)
    // =====================================================
    public Customer createCustomer(String name, String mobile, String address) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setMobile(mobile);
        customer.setAddress(address);
        customer.setCustomerCode(generateCustomerCode());
        return customerRepo.save(customer);
    }

    // =====================================================
    // ✅ READ APIs (LEGACY)
    // =====================================================
    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    public Customer getCustomerById(UUID id) {
        return customerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    // =====================================================
    // 📒 CUSTOMER LEDGER (ENTITY, ASC — LEGACY)
    // =====================================================
    public List<CustomerLedger> getCustomerLedger(UUID customerId) {
        return ledgerRepo.findByCustomer_IdOrderByCreatedAtAsc(customerId);
    }

    // =====================================================
    // 🔍 SEARCH (AUTOCOMPLETE)
    // =====================================================
    public List<Customer> search(String q) {
        return customerRepo.search(q, PageRequest.of(0, 10));
    }

    // =====================================================
    // 🟡 PENDING BILLS (LEDGER = TRUTH)
    // =====================================================
    public List<PendingBillResponse> getPendingBills(UUID customerId) {

        return ledgerRepo.findPendingBills(customerId).stream()
                .map(row -> {

                    UUID billId = (UUID) row[0];
                    String billCode = (String) row[1];
                    LocalDateTime createdAt = (LocalDateTime) row[2];
                    BigDecimal totalAmount = (BigDecimal) row[3];

                    BigDecimal dueAmount = ledgerRepo.getDueForBill(billId);
                    BigDecimal paidAmount = totalAmount.subtract(dueAmount);

                    PendingBillResponse r = new PendingBillResponse();
                    r.setBillId(billId);
                    r.setBillCode(billCode);
                    r.setBillDate(createdAt.toLocalDate());
                    r.setTotalAmount(totalAmount);
                    r.setPaidAmount(paidAmount);
                    r.setDueAmount(dueAmount);

                    return r;
                })
                .filter(r -> r.getDueAmount().signum() > 0)
                .toList();
    }

    // =====================================================
    // 🆕 CUSTOMER BALANCE (LEDGER TRUTH)
    // =====================================================
    public BigDecimal getCustomerBalance(UUID customerId) {
        return ledgerRepo.calculateBalance(customerId);
    }

    public Page<CustomerBalanceView> listCustomers(Pageable pageable) {
        return ledgerRepo.fetchCustomerBalances(pageable);
    }

    // =====================================================
    // 🆕 CUSTOMER STATEMENT (DTO, DESC)
    // =====================================================
    public List<CustomerStatementRowDto> getCustomerStatement(UUID customerId) {

        return ledgerRepo.findStatement(customerId).stream()
                .map(l -> {

                    BigDecimal debit = BigDecimal.ZERO;
                    BigDecimal credit = BigDecimal.ZERO;

                    if (l.getEntryType() == LedgerType.DEBIT) {
                        debit = l.getAmount();
                    } else {
                        credit = l.getAmount();
                    }

                    String ref =
                            l.getBill() != null
                                    ? l.getBill().getBillCode()
                                    : "ADJUSTMENT";

                    return new CustomerStatementRowDto(
                            l.getCreatedAt().toLocalDate(),
                            ref,
                            l.getEntryType().name(),
                            debit,
                            credit
                    );
                })
                .toList();
    }

    // =====================================================
    // 🆕 CUSTOMER LIST (COMPOSED, SAFE, UI-READY)
    // =====================================================
    public List<CustomerListResponse> getCustomerList() {

        Map<UUID, BigDecimal> balanceMap =
                ledgerRepo.calculateBalances()
                        .stream()
                        .collect(Collectors.toMap(
                                r -> (UUID) r[0],
                                r -> (BigDecimal) r[1]
                        ));

        Map<UUID, LocalDateTime> lastTxnMap =
                ledgerRepo.findLastTxnDates()
                        .stream()
                        .collect(Collectors.toMap(
                                r -> (UUID) r[0],
                                r -> (LocalDateTime) r[1]
                        ));

        return customerRepo.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(c -> new CustomerListResponse(
                        c.getId(),
                        c.getCustomerCode(),
                        c.getName(),
                        c.getMobile(),
                        c.getAddress(),
                        balanceMap
                                .getOrDefault(c.getId(), BigDecimal.ZERO)
                                .setScale(2, RoundingMode.HALF_UP),
                        lastTxnMap.get(c.getId())
                ))
                .toList();
    }

    // =====================================================
    // 🔢 CUSTOMER CODE GENERATOR
    // =====================================================
    private String generateCustomerCode() {
        long count = customerRepo.count() + 1;
        return String.format("CUS-%04d", count);
    }
}
