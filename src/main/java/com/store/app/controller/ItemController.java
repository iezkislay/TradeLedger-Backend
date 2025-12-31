package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.ItemSearchResponse;
import com.store.app.entity.Item;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.ItemService;
import jakarta.servlet.http.HttpSession;
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

    // ✅ Create Item (OWNER)
    @PostMapping
    public ResponseEntity<ApiResponse<Item>> createItem(
            @RequestBody Item item,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);

        Item saved = itemService.createItem(item, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, saved, "Item created")
        );
    }

    // ✅ Update Item (OWNER)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> updateItem(
            @PathVariable UUID id,
            @RequestBody Item item,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);

        Item updated = itemService.updateItem(id, item, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, updated, "Item updated successfully")
        );
    }

    /* =====================================================
       BILLING — SAFE SEARCH (READ ONLY)
       ===================================================== */

    /**
     * Used by billing UI only
     * - No mutation
     * - No auth role restriction (billing staff allowed)
     * - Min query length enforced to avoid table scans
     */
    @GetMapping("/search")
    public List<ItemSearchResponse> search(
            @RequestParam(required = false) String q
    ) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }

        return itemService.searchForBilling(q.trim());
    }
}
