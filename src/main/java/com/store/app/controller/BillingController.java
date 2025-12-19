package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.CreateBillRequest;
import com.store.app.entity.Bill;
import com.store.app.entity.User;
import com.store.app.repository.UserRepository;
import com.store.app.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bills")
public class BillingController {

    private final BillingService billingService;
    private final UserRepository userRepository;

    public BillingController(
            BillingService billingService,
            UserRepository userRepository
    ) {
        this.billingService = billingService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Bill>> createBill(
            @RequestBody CreateBillRequest request
    ) {
        // Phase-1: pick any existing user (replace with auth later)
        User user = userRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No user found in system"));

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
