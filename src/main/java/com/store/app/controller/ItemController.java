package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.CreateItemRequest;
import com.store.app.dto.ItemSearchResponse;
import com.store.app.entity.Item;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.ItemService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final AuthService authService;

    public ItemController(
            ItemService itemService,
            AuthService authService
    ) {
        this.itemService = itemService;
        this.authService = authService;
    }

    /* =====================================================
       ITEM MANAGEMENT (OWNER ONLY)
       ===================================================== */

    // ✅ Create Item (+ Optional Opening Stock) — EXISTING, DO NOT BREAK
    @PostMapping
    public ResponseEntity<ApiResponse<Item>> createItem(
            @RequestBody CreateItemRequest request,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);

        Item saved =
                itemService.createItemWithOpeningStock(request, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, saved, "Item created")
        );
    }

    // ✅ Update Item (OWNER) — EXISTING, UNCHANGED
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> updateItem(
            @PathVariable UUID id,
            @RequestBody Item item,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);

        Item updated =
                itemService.updateItem(id, item, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, updated, "Item updated successfully")
        );
    }

    /* =====================================================
       🔍 BILLING — SAFE SEARCH (READ ONLY)
       ===================================================== */

    @GetMapping("/search")
    public List<ItemSearchResponse> search(
            @RequestParam(required = false) String q,
            HttpSession session
    ) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }

        User user = authService.getCurrentUser(session);
        return itemService.searchForBilling(q.trim(), user);
    }

    /* =====================================================
       📦 LIST ALL ITEMS (EXISTING — DO NOT BREAK)
       ===================================================== */

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemSearchResponse>>> listAll(
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);

        // 🔒 Billing or Owner allowed
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        itemService.listAllItems(user),
                        "Items loaded"
                )
        );
    }

    /* =====================================================
       📄 PAGED ITEMS LIST (ENHANCED)
       ===================================================== */

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<Page<ItemSearchResponse>>> listPaged(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "NAME") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);

        // 🔒 Billing or Owner allowed
        authService.requireBillingOrOwner(user);

        Page<ItemSearchResponse> result =
                itemService.listItemsPaged(
                        (q == null || q.isBlank()) ? null : q.trim(),
                        (category == null || category.isBlank()) ? null : category.trim(),
                        sortBy,
                        direction,
                        PageRequest.of(page, size),
                        user
                );

        return ResponseEntity.ok(
                new ApiResponse<>(true, result, "Items loaded")
        );
    }

    /* =====================================================
       📦 GET SINGLE ITEM (NEW — SAFE ADDITION)
       ===================================================== */

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemSearchResponse>> getItemById(
            @PathVariable UUID id,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);

        // 🔒 Billing or Owner allowed
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        itemService.getItemById(id, user),
                        "Item fetched"
                )
        );
    }
}
