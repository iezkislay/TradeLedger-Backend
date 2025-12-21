package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.RefundRequest;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.RefundService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;
    private final AuthService authService;

    public RefundController(
            RefundService refundService,
            AuthService authService
    ) {
        this.refundService = refundService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> refund(
            @RequestBody RefundRequest request,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        refundService.processRefund(request, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Refund processed")
        );
    }
}
