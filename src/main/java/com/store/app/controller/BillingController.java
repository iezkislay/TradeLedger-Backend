package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.CreateBillRequest;
import com.store.app.entity.Bill;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.BillingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bills")
public class BillingController {

    private final BillingService billingService;
    private final AuthService authService;

    public BillingController(
            BillingService billingService,
            AuthService authService
    ) {
        this.billingService = billingService;
        this.authService = authService;
    }

    // ✅ Create Bill (OWNER / BILLING)
    @PostMapping
    public ResponseEntity<ApiResponse<Bill>> createBill(
            @RequestBody CreateBillRequest request,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        Bill bill = billingService.createBill(request, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, bill, "Bill created successfully")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> getBill(@PathVariable UUID id) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, null, "Get bill by ID (Phase-2)")
        );
    }
}
