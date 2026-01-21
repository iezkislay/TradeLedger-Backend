package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.LedgerBillSummary;
import com.store.app.enums.LedgerType;
import com.store.app.entity.User;
import com.store.app.repository.CustomerLedgerRepository;
import com.store.app.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final CustomerLedgerRepository ledgerRepo;
    private final AuthService authService;

    public LedgerController(
            CustomerLedgerRepository ledgerRepo,
            AuthService authService
    ) {
        this.ledgerRepo = ledgerRepo;
        this.authService = authService;
    }

    /**
     * 📒 Ledger Summary (Bill-scoped)
     * Explains why balance looks like it does
     */
    @GetMapping("/bill/{billId}/summary")
    public ResponseEntity<ApiResponse<LedgerBillSummary>> getBillLedgerSummary(
            @PathVariable UUID billId,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        BigDecimal debit =
                ledgerRepo.sumByBillAndType(billId, LedgerType.DEBIT);

        BigDecimal credit =
                ledgerRepo.sumByBillAndType(billId, LedgerType.CREDIT);

        BigDecimal returnCredit =
                ledgerRepo.sumByBillAndType(billId, LedgerType.RETURN_CREDIT);

        BigDecimal adjustment =
                ledgerRepo.sumByBillAndType(billId, LedgerType.ADJUSTMENT);

        debit = debit == null ? BigDecimal.ZERO : debit;
        credit = credit == null ? BigDecimal.ZERO : credit;
        returnCredit = returnCredit == null ? BigDecimal.ZERO : returnCredit;
        adjustment = adjustment == null ? BigDecimal.ZERO : adjustment;

        BigDecimal netBalance = debit
                .subtract(credit)
                .subtract(returnCredit)
                .subtract(adjustment);

        LedgerBillSummary summary = new LedgerBillSummary(
                debit,
                credit,
                returnCredit,
                adjustment,
                netBalance
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        summary,
                        "Ledger summary fetched"
                )
        );
    }

    /* =====================================================
       🔒 INTERNAL USE — SERVICE LAYER ONLY
       (NO API EXPOSURE)
       ===================================================== */
    public LedgerBillSummary getBillLedgerSummaryInternal(UUID billId) {
        return getBillLedgerSummary(billId, null)
                .getBody()
                .getData();
    }
}
