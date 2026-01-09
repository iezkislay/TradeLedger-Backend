package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.BillItemResponse;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.BillItemService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bill-items")
@RequiredArgsConstructor
public class BillItemController {

    private final BillItemService billItemService;
    private final AuthService authService;

    /* =====================================================
       🧾 GET BILL ITEM BY ID
       ===================================================== */

    @GetMapping("/{billItemId}")
    public ResponseEntity<ApiResponse<BillItemResponse>> getBillItem(
            @PathVariable UUID billItemId,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        billItemService.getBillItem(billItemId),
                        "Bill item loaded"
                )
        );
    }
}
