package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.BillAuditResponse;
import com.store.app.dto.BillListResponse;
import com.store.app.dto.BillOverrideRequest;
import com.store.app.dto.BillPrintResponse;
import com.store.app.dto.BillResponse;
import com.store.app.dto.CreateBillRequest;
import com.store.app.dto.SettleBillRequest;
import com.store.app.entity.Bill;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.BillingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bills")
public class BillingController {

    private final BillingService billingService;
    private final AuthService authService;

    public BillingController(
            BillingService billingService,
            AuthService authService
    ) {
        this.billingService = billingService;
        this.authService = authService;
    }

    // =====================================================
    // 📋 LIST / SEARCH BILLS (READ-ONLY)
    // =====================================================
    @GetMapping
    public ResponseEntity<ApiResponse<List<BillListResponse>>> listBills(
            @RequestParam(required = false) String search,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        billingService.listBills(search),
                        "Bills loaded"
                )
        );
    }

    // =====================================================
    // ✅ CREATE BILL (OWNER / BILLING)
    // =====================================================
    @PostMapping
    public ResponseEntity<ApiResponse<Bill>> createBill(
            @RequestBody CreateBillRequest request,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        Bill bill = billingService.createBill(request, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, bill, "Bill created successfully")
        );
    }

    // =====================================================
    // ✅ GET BILL BY ID (READ-SAFE)
    // =====================================================
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> getBill(
            @PathVariable UUID id,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        billingService.getBillById(id),
                        "Bill loaded"
                )
        );
    }

    // =====================================================
    // 🖨️ PRINT BILL (READ-ONLY, ROLE PROTECTED)
    // =====================================================
    @GetMapping("/{id}/print")
    public ResponseEntity<BillPrintResponse> printBill(
            @PathVariable UUID id,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        return ResponseEntity.ok(
                billingService.getBillForPrint(id)
        );
    }

    // =====================================================
    // 💰 SETTLE BILL (PARTIAL / FINAL / WAIVER)
    // =====================================================
    @PostMapping("/{billId}/settle")
    public ResponseEntity<ApiResponse<Void>> settleBill(
            @PathVariable UUID billId,
            @RequestBody SettleBillRequest request,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        billingService.settleBill(billId, request, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, null, "Bill settled successfully")
        );
    }

    // =====================================================
    // 🆕 OVERRIDE BILL TOTAL (OWNER / BILLING)
    // =====================================================
    @PostMapping("/{id}/override")
    public ResponseEntity<ApiResponse<String>> overrideBill(
            @PathVariable UUID id,
            @RequestBody BillOverrideRequest request,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        billingService.overrideBillPrice(id, request, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Bill overridden successfully")
        );
    }

    // =====================================================
    // 🔒 CLOSE BILL (FINAL)
    // =====================================================
    @PostMapping("/{billId}/close")
    public ResponseEntity<ApiResponse<String>> closeBill(
            @PathVariable UUID billId,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        billingService.closeBill(billId, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Bill closed successfully")
        );
    }

    // =====================================================
    // 📜 BILL AUDIT (READ-ONLY)
    // =====================================================
    @GetMapping("/{billId}/audit")
    public ResponseEntity<ApiResponse<BillAuditResponse>> audit(
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
                        billingService.getBillAudit(billId, user),
                        "Bill audit loaded"
                )
        );
    }
}
