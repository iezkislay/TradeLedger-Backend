package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.BillListResponse;
import com.store.app.dto.CustomerBalanceView;
import com.store.app.dto.CustomerListResponse;
import com.store.app.dto.CustomerStatementRowDto;
import com.store.app.dto.PendingBillResponse;
import com.store.app.entity.Customer;
import com.store.app.entity.CustomerLedger;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final AuthService authService;

    // =====================================================
    // ✅ CREATE CUSTOMER
    // =====================================================
    @PostMapping
    public ResponseEntity<ApiResponse<Customer>> createCustomer(
            @RequestBody Customer customer,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        customerService.createCustomer(customer, user),
                        "Customer created successfully"
                )
        );
    }

    // =====================================================
    // ✅ GET ALL CUSTOMERS (LEGACY – ENTITY LIST)
    // ⚠️ DO NOT REMOVE (USED BY AUTOCOMPLETE / OLD UI)
    // =====================================================
    @GetMapping
    public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomers(
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        customerService.getAllCustomers(),
                        "Customers fetched"
                )
        );
    }

    // =====================================================
    // 🆕 GET CUSTOMER LIST (COMPOSED, BALANCE + ADDRESS)
    // =====================================================
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<CustomerListResponse>>> listCustomers(
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        customerService.getCustomerList(),
                        "Customer list fetched"
                )
        );
    }

    // =====================================================
    // 🆕 GET CUSTOMERS WITH BALANCE (PAGINATED)
    // =====================================================
    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<Page<CustomerBalanceView>>> listCustomersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        customerService.listCustomers(PageRequest.of(page, size)),
                        "Customers with balance fetched"
                )
        );
    }

    // =====================================================
    // ✅ GET CUSTOMER BY ID
    // =====================================================
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(
            @PathVariable UUID id,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        customerService.getCustomerById(id),
                        "Customer fetched"
                )
        );
    }

    // =====================================================
    // 📒 CUSTOMER LEDGER (ENTITY, ASC — LEGACY)
    // =====================================================
    @GetMapping("/{id}/ledger")
    public ResponseEntity<ApiResponse<List<CustomerLedger>>> getCustomerLedger(
            @PathVariable UUID id,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        customerService.getCustomerLedger(id),
                        "Customer ledger fetched"
                )
        );
    }

    // =====================================================
    // 🔍 CUSTOMER SEARCH (AUTOCOMPLETE)
    // =====================================================
    @GetMapping("/search")
    public List<Customer> search(
            @RequestParam String q,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        if (q == null || q.trim().length() < 2) {
            return List.of();
        }

        return customerService.search(q.trim());
    }

    // =====================================================
    // 🟡 PENDING BILLS (LEDGER = TRUTH)
    // =====================================================
    @GetMapping("/{customerId}/pending-bills")
    public ResponseEntity<ApiResponse<List<PendingBillResponse>>> getPendingBills(
            @PathVariable UUID customerId,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        customerService.getPendingBills(customerId),
                        "Pending bills loaded"
                )
        );
    }

    // =====================================================
    // 🆕 CUSTOMER BILLS (READ-ONLY)
    // =====================================================
    @GetMapping("/{id}/bills")
    public ResponseEntity<ApiResponse<List<BillListResponse>>> getCustomerBills(
            @PathVariable UUID id,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        customerService.getCustomerBills(id),
                        "Customer bills fetched"
                )
        );
    }

    // =====================================================
    // 🆕 CUSTOMER BALANCE (LEDGER TRUTH)
    // =====================================================
    @GetMapping("/{id}/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getCustomerBalance(
            @PathVariable UUID id,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        customerService.getCustomerBalance(id),
                        "Customer balance fetched"
                )
        );
    }

    // =====================================================
    // 🆕 CUSTOMER STATEMENT (DTO, DESC)
    // =====================================================
    @GetMapping("/{id}/statement")
    public ResponseEntity<ApiResponse<List<CustomerStatementRowDto>>> getCustomerStatement(
            @PathVariable UUID id,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        customerService.getCustomerStatement(id),
                        "Customer statement fetched"
                )
        );
    }
}
