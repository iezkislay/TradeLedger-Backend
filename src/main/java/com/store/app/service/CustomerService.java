package com.store.app.service;

import com.store.app.entity.Customer;
import com.store.app.entity.CustomerLedger;
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

    public CustomerService(
            CustomerRepository customerRepo,
            CustomerLedgerRepository ledgerRepo
    ) {
        this.customerRepo = customerRepo;
        this.ledgerRepo = ledgerRepo;
    }

    // ✅ Create customer with auto-generated customerCode
    public Customer createCustomer(Customer customer) {

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

    // 🆕 Get customers with outstanding balance (credit customers)
    public List<Customer> getOutstandingCustomers() {
        return customerRepo.findByBalanceGreaterThanOrderByBalanceDesc(BigDecimal.ZERO);
    }

    // 🆕 Get full ledger for a customer (chronological order)
    public List<CustomerLedger> getCustomerLedger(UUID customerId) {
        return ledgerRepo.findByCustomer_IdOrderByCreatedAtAsc(customerId);
    }

    // 🔢 Customer code generator
    private String generateCustomerCode() {
        long count = customerRepo.count() + 1;
        return String.format("CUS-%04d", count);
    }
}
