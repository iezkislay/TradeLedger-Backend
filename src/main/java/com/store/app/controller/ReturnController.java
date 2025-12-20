package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.PartialReturnRequest;
import com.store.app.entity.User;
import com.store.app.repository.UserRepository;
import com.store.app.service.ReturnService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final ReturnService returnService;
    private final UserRepository userRepository;

    public ReturnController(
            ReturnService returnService,
            UserRepository userRepository
    ) {
        this.returnService = returnService;
        this.userRepository = userRepository;
    }

    /**
     * 🔁 Partial Return (DELIVERED or PENDING)
     *
     * POST /api/returns/partial
     */
    @PostMapping("/partial")
    public ResponseEntity<ApiResponse<String>> partialReturn(
            @RequestBody PartialReturnRequest request
    ) {
        // 🔐 Phase-1 mock auth (same as billing / fulfilment)
        User user = userRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No user found"));

        returnService.partialReturn(request, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Partial return processed successfully")
        );
    }
}
