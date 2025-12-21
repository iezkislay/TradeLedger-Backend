package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.entity.BillItem;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.FulfilmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fulfilments")
public class FulfilmentController {

    private final FulfilmentService fulfilmentService;
    private final AuthService authService;

    public FulfilmentController(
            FulfilmentService fulfilmentService,
            AuthService authService
    ) {
        this.fulfilmentService = fulfilmentService;
        this.authService = authService;
    }

    // ✅ Fulfil pending quantity (OWNER / BILLING)
    @PostMapping("/{billItemId}")
    public ResponseEntity<ApiResponse<String>> fulfil(
            @PathVariable UUID billItemId,
            @RequestParam BigDecimal quantity,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        fulfilmentService.fulfil(billItemId, quantity, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Item fulfilled successfully")
        );
    }

    // 📊 Pending fulfilment report (OWNER / BILLING)
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<BillItem>>> pending(
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        fulfilmentService.getPendingFulfilments(),
                        "Pending fulfilments fetched"
                )
        );
    }
}
