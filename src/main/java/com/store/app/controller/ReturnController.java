package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.PartialReturnRequest;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.ReturnService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final ReturnService returnService;
    private final AuthService authService;

    public ReturnController(
            ReturnService returnService,
            AuthService authService
    ) {
        this.returnService = returnService;
        this.authService = authService;
    }

    /**
     * 🔁 Partial Return (DELIVERED or PENDING)
     * POST /api/returns/partial
     */
    @PostMapping("/partial")
    public ResponseEntity<ApiResponse<String>> partialReturn(
            @RequestBody PartialReturnRequest request,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        returnService.partialReturn(request, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Partial return processed successfully")
        );
    }
}
