package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.entity.Customer;
import com.store.app.entity.CustomerLedger;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final AuthService authService;

    // ✅ Create Customer (OWNER / BILLING)
    @PostMapping
    public ResponseEntity<ApiResponse<Customer>> createCustomer(
            @RequestBody Customer customer,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);

        Customer saved = customerService.createCustomer(customer, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, saved, "Customer created successfully")
        );
    }

    // ✅ Get all customers (read-only)
    @GetMapping
    public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomers() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, customerService.getAllCustomers(), "Customers fetched")
        );
    }

    // ✅ Get customer by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(
            @PathVariable UUID id
    ) {
        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, customer, "Customer fetched")
        );
    }

    // 🆕 Outstanding customers
    @GetMapping("/outstanding")
    public ResponseEntity<ApiResponse<List<Customer>>> getOutstandingCustomers() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        customerService.getOutstandingCustomers(),
                        "Outstanding customers fetched"
                )
        );
    }

    // 🆕 Customer ledger
    @GetMapping("/{id}/ledger")
    public ResponseEntity<ApiResponse<List<CustomerLedger>>> getCustomerLedger(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        customerService.getCustomerLedger(id),
                        "Customer ledger fetched"
                )
        );
    }
}
