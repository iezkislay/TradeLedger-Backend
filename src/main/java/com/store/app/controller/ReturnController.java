package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.CreateReturnNoteRequest;
import com.store.app.dto.ReturnItemRequest;
import com.store.app.entity.ReturnNote;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.ReturnService;
import com.store.app.repository.ReturnNoteRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final ReturnService returnService;
    private final ReturnNoteRepository returnNoteRepo;
    private final AuthService authService;

    public ReturnController(
            ReturnService returnService,
            ReturnNoteRepository returnNoteRepo,
            AuthService authService
    ) {
        this.returnService = returnService;
        this.returnNoteRepo = returnNoteRepo;
        this.authService = authService;
    }

    /* =====================================================
       🆕 CREATE RETURN NOTE (STEP 1 — VALUATION ONLY)
       ===================================================== */

    /**
     * POST /api/returns
     * Creates a return note with valuation
     * (NO ledger, NO refund, NO settlement)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UUID>> createReturnNote(
            @RequestBody CreateReturnNoteRequest request,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        ReturnNote note = returnService.createReturnNote(
                request.getItems(),
                user
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        note.getId(),
                        "Return note created successfully"
                )
        );
    }

    /* =====================================================
       ✅ FINALIZE RETURN NOTE (STEP 2 — IDEMPOTENT)
       ===================================================== */

    /**
     * POST /api/returns/{returnNoteId}/finalize
     * Applies:
     * 1. Credit to bill due
     * 2. Refund excess (if any)
     */
    @PostMapping("/{returnNoteId}/finalize")
    public ResponseEntity<ApiResponse<String>> finalizeReturn(
            @PathVariable UUID returnNoteId,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        returnService.finalizeReturn(returnNoteId, user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "OK",
                        "Return finalized successfully"
                )
        );
    }

    /* =====================================================
       🧱 HARDENING LAYER 4 — CONTROLLER GUARD (FUTURE USE)
       ===================================================== */

    /**
     * Example guard for future update/delete APIs
     * (DO NOT expose mutations after finalization)
     */
    private ResponseEntity<ApiResponse<?>> rejectIfFinalized(UUID returnNoteId) {

        ReturnNote note = returnNoteRepo.findById(returnNoteId)
                .orElseThrow(() -> new RuntimeException("Return note not found"));

        if (note.isFinalized()) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(
                            false,
                            null,
                            "Return already finalized"
                    )
            );
        }

        return null; // caller proceeds
    }
}
