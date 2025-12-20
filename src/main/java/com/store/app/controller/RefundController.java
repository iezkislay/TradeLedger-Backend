package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.RefundRequest;
import com.store.app.entity.User;
import com.store.app.repository.UserRepository;
import com.store.app.service.RefundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;
    private final UserRepository userRepo;

    public RefundController(
            RefundService refundService,
            UserRepository userRepo
    ) {
        this.refundService = refundService;
        this.userRepo = userRepo;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> refund(
            @RequestBody RefundRequest request
    ) {
        // Phase-2 mock auth
        User user = userRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No user found"));

        refundService.processRefund(request, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Refund processed")
        );
    }
}
