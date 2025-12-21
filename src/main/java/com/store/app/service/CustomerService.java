package com.store.app.service;

import com.store.app.entity.Customer;
import com.store.app.entity.CustomerLedger;
import com.store.app.entity.User;
import com.store.app.repository.CustomerLedgerRepository;
import com.store.app.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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

    // ✅ Create customer (OWNER / BILLING)
    public Customer createCustomer(Customer customer, User user) {

        // 🔒 RBAC
        authService.requireBillingOrOwner(user);

        String customerCode = generateCustomerCode();
        customer.setCustomerCode(customerCode);

        return customerRepo.save(customer);
    }

    // ✅ Get all customers
    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    // ✅ Get customer by ID
    public Customer getCustomerById(UUID id) {
        return customerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    // 🆕 Customers with outstanding balance
    public List<Customer> getOutstandingCustomers() {
        return customerRepo
                .findByBalanceGreaterThanOrderByBalanceDesc(BigDecimal.ZERO);
    }

    // 🆕 Customer ledger
    public List<CustomerLedger> getCustomerLedger(UUID customerId) {
        return ledgerRepo
                .findByCustomer_IdOrderByCreatedAtAsc(customerId);
    }

    // 🔢 Customer code generator
    private String generateCustomerCode() {
        long count = customerRepo.count() + 1;
        return String.format("CUS-%04d", count);
    }
}
