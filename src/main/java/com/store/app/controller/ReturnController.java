package com.store.app.controller;

import com.store.app.dto.*;
import com.store.app.entity.ReturnItem;
import com.store.app.entity.ReturnNote;
import com.store.app.entity.User;
import com.store.app.repository.ReturnItemRepository;
import com.store.app.repository.ReturnNoteRepository;
import com.store.app.service.AuthService;
import com.store.app.service.ReturnService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.store.app.repository.RefundRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final ReturnService returnService;
    private final ReturnNoteRepository returnNoteRepo;
    private final ReturnItemRepository returnItemRepo;
    private final RefundRepository refundRepo;
    private final AuthService authService;

    public ReturnController(
            ReturnService returnService,
            ReturnNoteRepository returnNoteRepo,
            ReturnItemRepository returnItemRepo,
            RefundRepository refundRepo,
            AuthService authService
    ) {
        this.returnService = returnService;
        this.returnNoteRepo = returnNoteRepo;
        this.returnItemRepo = returnItemRepo;
        this.refundRepo = refundRepo;
        this.authService = authService;
    }

    /* =====================================================
       🆕 CREATE RETURN NOTE (STEP 1 — VALUATION ONLY)
       ===================================================== */

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

    // PRINT RETURN
    @GetMapping("/{id}/print")
    public ResponseEntity<ReturnPrintResponse> printReturn(
            @PathVariable UUID id,
            HttpSession session
    ) {

        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                returnService.getReturnForPrint(id)
        );
    }

    /* =====================================================
       📦 RETURNS (GOODS MOVEMENT TRUTH — READ ONLY)
       ===================================================== */

    @GetMapping
    public ResponseEntity<ApiResponse<GroupedReturnsResponse>> getReturns(
            @RequestParam UUID billId,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        // =========================
        // FETCH RETURN NOTES
        // =========================
        List<ReturnNote> notes =
                returnNoteRepo.findByBill_Id(billId);

        List<ReturnNoteView> returnViews = notes.stream().map(note -> {

            // =========================
            // ITEMS
            // =========================
            List<ReturnItem> items =
                    returnItemRepo.findByReturnNote_Id(note.getId());

            List<ReturnItemView> itemViews = items.stream().map(ri -> {

                BigDecimal delivered =
                        ri.getReturnedDeliveredQty() == null
                                ? BigDecimal.ZERO
                                : ri.getReturnedDeliveredQty();

                BigDecimal pending =
                        ri.getReturnedPendingQty() == null
                                ? BigDecimal.ZERO
                                : ri.getReturnedPendingQty();

                BigDecimal qty = delivered.add(pending);
                BigDecimal gross = qty.multiply(ri.getPrice());

                BigDecimal effective =
                        note.getReturnedGrossAmount().signum() == 0
                                ? BigDecimal.ZERO
                                : gross
                                .multiply(note.getNetReturnAmount())
                                .divide(
                                        note.getReturnedGrossAmount(),
                                        2,
                                        RoundingMode.HALF_UP
                                );

                String returnType =
                        delivered.signum() > 0 ? "DELIVERED" : "PENDING";

                return new ReturnItemView(
                        ri.getBillItem().getId(),
                        ri.getItem().getItemCode(),
                        ri.getItem().getName(),

                        qty,
                        gross,
                        effective,

                        returnType,
                        ri.getCreatedAt()
                );
            }).toList();

            // =========================
            // REFUNDS
            // =========================
            BigDecimal alreadyRefunded =
                    refundRepo.sumRefundedAmountByReturnNote(note.getId());

            if (alreadyRefunded == null) {
                alreadyRefunded = BigDecimal.ZERO;
            }

            BigDecimal refundableRemaining =
                    note.getNetReturnAmount().subtract(alreadyRefunded);

            if (refundableRemaining.signum() < 0) {
                refundableRemaining = BigDecimal.ZERO;
            }

            return new ReturnNoteView(
                    note.getId(),
                    note.isFinalized(),

                    note.getReturnedGrossAmount(),
                    note.getNetReturnAmount(),

                    alreadyRefunded,
                    refundableRemaining,

                    note.isResidualAdjusted(),      // ✅
                    note.getAdjustmentNote(),

                    itemViews
            );
        }).toList();

        // =========================
        // AGGREGATES (UNCHANGED)
        // =========================
        Object[] totals =
                (Object[]) returnNoteRepo.getReturnAggregates(billId);

        BigDecimal returnedGrossTotal =
                (BigDecimal) totals[0];

        BigDecimal returnedEffectiveTotal =
                (BigDecimal) totals[1];

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        new GroupedReturnsResponse(
                                returnViews,
                                returnedGrossTotal,
                                returnedEffectiveTotal
                        ),
                        "Returns fetched"
                )
        );
    }


    /* =====================================================
       🧱 HARDENING LAYER 4 — CONTROLLER GUARD (FUTURE USE)
       ===================================================== */

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

        return null;
    }
}
