package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.entity.BillItem;
import com.store.app.entity.User;
import com.store.app.repository.BillItemRepository;
import com.store.app.repository.UserRepository;
import com.store.app.service.ReturnService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final ReturnService returnService;
    private final BillItemRepository billItemRepo;
    private final UserRepository userRepository;

    public ReturnController(
            ReturnService returnService,
            BillItemRepository billItemRepo,
            UserRepository userRepository
    ) {
        this.returnService = returnService;
        this.billItemRepo = billItemRepo;
        this.userRepository = userRepository;
    }

    @PostMapping("/bill-item/{billItemId}")
    public ResponseEntity<ApiResponse<String>> returnAgainstBill(
            @PathVariable UUID billItemId,
            @RequestParam BigDecimal quantity
    ) {
        User user = userRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No user found"));

        BillItem billItem = billItemRepo.findById(billItemId)
                .orElseThrow(() -> new RuntimeException("Bill item not found"));

        returnService.returnItem(billItem, quantity, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Item returned successfully")
        );
    }
}
