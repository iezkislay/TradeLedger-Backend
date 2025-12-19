package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.entity.Item;
import com.store.app.entity.User;
import com.store.app.repository.UserRepository;
import com.store.app.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final UserRepository userRepository;

    public ItemController(
            ItemService itemService,
            UserRepository userRepository
    ) {
        this.itemService = itemService;
        this.userRepository = userRepository;
    }

    // ✅ Create Item (OWNER)
    @PostMapping
    public ResponseEntity<ApiResponse<Item>> createItem(
            @RequestBody Item item
    ) {
        User user = userRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No user found"));

        Item saved = itemService.createItem(item, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, saved, "Item created")
        );
    }

    // ✅ Update Item (OWNER)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> updateItem(
            @PathVariable UUID id,
            @RequestBody Item item
    ) {
        User user = userRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No user found"));

        Item updated = itemService.updateItem(id, item, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, updated, "Item updated successfully")
        );
    }
}
