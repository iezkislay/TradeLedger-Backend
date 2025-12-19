package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.entity.BillItem;
import com.store.app.entity.User;
import com.store.app.repository.UserRepository;
import com.store.app.service.FulfilmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fulfilments")
public class FulfilmentController {

    private final FulfilmentService fulfilmentService;
    private final UserRepository userRepo;

    public FulfilmentController(
            FulfilmentService fulfilmentService,
            UserRepository userRepo
    ) {
        this.fulfilmentService = fulfilmentService;
        this.userRepo = userRepo;
    }

    // ✅ Fulfil pending quantity
    @PostMapping("/{billItemId}")
    public ResponseEntity<ApiResponse<String>> fulfil(
            @PathVariable UUID billItemId,
            @RequestParam BigDecimal quantity
    ) {
        User user = userRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No user found"));

        fulfilmentService.fulfil(billItemId, quantity, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Item fulfilled successfully")
        );
    }

    // 📊 Pending fulfilment report
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<BillItem>>> pending() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        fulfilmentService.getPendingFulfilments(),
                        "Pending fulfilments fetched"
                )
        );
    }
}
