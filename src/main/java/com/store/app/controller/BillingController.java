package com.store.app.controller;

import com.store.app.dto.*;
import com.store.app.entity.Bill;
import com.store.app.entity.User;
import com.store.app.dto.ActivateBillRequest;
import com.store.app.service.AuthService;
import com.store.app.service.BillingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    // =====================================================
    // 📜 Create ESTIMATE Bill
    // =====================================================

    @PostMapping("/estimate")
    public ResponseEntity<ApiResponse<Bill>> createEstimate(
            @RequestBody CreateBillRequest request,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        Bill bill = billingService.createEstimate(request, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, bill, "Estimate created")
        );
    }

    // =====================================================
    // 📜 Activate ESTIMATE → ACTIVE
    // =====================================================

    @PostMapping("/{billId}/activate")
    public ResponseEntity<ApiResponse<String>> activateEstimate(
            @PathVariable UUID billId,
            @RequestBody ActivateBillRequest req,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        billingService.activateEstimate(
                billId,
                req.getPaymentType(),
                req.getAmountPaid(),
                user
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Bill activated")
        );
    }

    // =====================================================
    // ❌ CANCEL ESTIMATE
    // =====================================================
    @PostMapping("/{billId}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelEstimate(
            @PathVariable UUID billId,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        billingService.cancelEstimate(billId, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Estimate cancelled")
        );
    }

    // =====================================================
    // GST BILLING
    // =====================================================
    @PostMapping("/gst/bills")
    public ResponseEntity<ApiResponse<Bill>> createGstBill(
            @RequestBody CreateBillRequest req,
            HttpSession session
    ) {
        System.out.println("🔥 GST API HIT");

        User user = authService.getCurrentUser(session);
        Bill bill = billingService.createGstBill(req, user);

        return ResponseEntity.ok(new ApiResponse<>(true, bill, "Success"));
    }


    // =====================================================
    // PRINT GST BILL
    // =====================================================
    @GetMapping("/gst/bills/{billId}/print")
    public ResponseEntity<ApiResponse<GstBillPrintResponse>> printGstBill(
            @PathVariable UUID billId
    ) {

        GstBillPrintResponse res = billingService.getGstBillForPrint(billId);

        return ResponseEntity.ok(new ApiResponse<>(true, res, "Success"));
        // return ResponseEntity.ok(ApiResponse.success(res));
    }


    // ====================================================
    // PUBLIC BILL FETCH
    // ====================================================

    @RestController
    @RequestMapping("/api/public/bills")
    public class BillingPublicController {

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<BillResponse>> getPublicBill(
                @PathVariable UUID id
        ) {
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            billingService.getBillById(id),
                            "Bill fetched"
                    )
            );
        }

        @GetMapping("/{id}/print")
        public ResponseEntity<BillPrintResponse> publicPrintBill(
                @PathVariable UUID id
        ) {
            return ResponseEntity.ok(
                    billingService.getBillForPrint(id)
            );
        }

        @GetMapping("/{id}/pdf")
        public ResponseEntity<byte[]> getPdf(@PathVariable UUID id) throws Exception {

            byte[] pdf = billingService.generateBillPdf(id);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=bill.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        }
    }
}
