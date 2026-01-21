package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.BillSummaryResponse;
import com.store.app.dto.CreateRefundRequest;
import com.store.app.dto.RefundRequest;
import com.store.app.dto.RefundView;
import com.store.app.entity.Refund;
import com.store.app.entity.User;
import com.store.app.repository.RefundRepository;
import com.store.app.service.AuthService;
import com.store.app.service.RefundService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;
    private final RefundRepository refundRepo;
    private final AuthService authService;

    public RefundController(
            RefundService refundService,
            RefundRepository refundRepo,
            AuthService authService
    ) {
        this.refundService = refundService;
        this.refundRepo = refundRepo;
        this.authService = authService;
    }

    /* =====================================================
       💸 REFUND — LEGACY (AGAINST BILL)
       ===================================================== */

    @PostMapping
    public ResponseEntity<ApiResponse<String>> refund(
            @RequestBody RefundRequest request,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        // 🔒 OWNER ONLY (UNCHANGED)
        refundService.processRefund(request, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Refund processed")
        );
    }

    /* =====================================================
       💸 REFUND — STEP B (AGAINST RETURN)
       ===================================================== */

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<String>> refundAgainstReturn(
            @RequestBody CreateRefundRequest request,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        // 🔒 BILLING + OWNER
        authService.requireBillingOrOwner(user);

        refundService.refundAgainstReturn(request, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Refund processed successfully")
        );
    }

    /* =====================================================
       🧾 BILL SUMMARY — RETURNS + REFUNDS + OVERRIDE AWARE
       ===================================================== */

    @GetMapping("/{billId}/summary")
    public ResponseEntity<ApiResponse<BillSummaryResponse>> getBillSummary(
            @PathVariable UUID billId,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        refundService.getBillSummary(billId, user),
                        "Bill summary loaded"
                )
        );
    }

    /* =====================================================
       💸 REFUNDS (MONEY OUT TRUTH — READ ONLY)
       ===================================================== */

    /**
     * GET /api/refunds?billId=...
     * Actual money given back to customer
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RefundView>>> getRefunds(
            @RequestParam UUID billId,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        List<Refund> refunds =
                refundRepo.findAllByBillId(billId);

        List<RefundView> result = refunds.stream()
                .map(r -> new RefundView(
                        r.getId(),
                        r.getAmount(),
                        r.getRefundMode() != null
                                ? r.getRefundMode().name()
                                : null,
                        r.getReason(),
                        r.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        result,
                        "Refunds fetched"
                )
        );
    }
}
