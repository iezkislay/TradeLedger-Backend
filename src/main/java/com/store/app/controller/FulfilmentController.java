package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.FulfilmentView;
import com.store.app.entity.BillItem;
import com.store.app.entity.User;
import com.store.app.repository.BillItemRepository;
import com.store.app.repository.ReturnItemRepository;
import com.store.app.repository.StockTransactionRepository;
import com.store.app.service.AuthService;
import com.store.app.service.FulfilmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.store.app.dto.PendingFulfilmentBillGroupView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fulfilments")
public class FulfilmentController {

    private final FulfilmentService fulfilmentService;
    private final AuthService authService;

    // READ-SIDE repositories
    private final BillItemRepository billItemRepo;
    private final ReturnItemRepository returnItemRepo;
    private final StockTransactionRepository stockTxnRepo;

    public FulfilmentController(
            FulfilmentService fulfilmentService,
            AuthService authService,
            BillItemRepository billItemRepo,
            ReturnItemRepository returnItemRepo,
            StockTransactionRepository stockTxnRepo
    ) {
        this.fulfilmentService = fulfilmentService;
        this.authService = authService;
        this.billItemRepo = billItemRepo;
        this.returnItemRepo = returnItemRepo;
        this.stockTxnRepo = stockTxnRepo;
    }

    // ✅ Fulfil pending quantity (OWNER / BILLING)
    @PostMapping("/{billItemId}")
    public ResponseEntity<ApiResponse<String>> fulfil(
            @PathVariable UUID billItemId,
            @RequestParam BigDecimal quantity,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        fulfilmentService.fulfil(billItemId, quantity, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Item fulfilled successfully")
        );
    }

    // 📊 Pending fulfilment (OWNER / BILLING)
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PendingFulfilmentBillGroupView>>> pending(
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        fulfilmentService.getPendingGrouped(),
                        "Pending fulfilments fetched"
                )
        );
    }

    /**
     * 📦 Fulfilments (Delivery Truth)
     * What physically went out for a bill
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FulfilmentView>>> getFulfilments(
            @RequestParam UUID billId,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        List<BillItem> items =
                billItemRepo.findByBillIdOrderByLineNumberAsc(billId);

        List<FulfilmentView> result = items.stream()
                .map(bi -> {

                    BigDecimal returnedQty =
                            returnItemRepo.getTotalReturnedQtyForBillItem(bi.getId());

                    if (returnedQty == null) {
                        returnedQty = BigDecimal.ZERO;
                    }

                    String status = deriveStatus(bi, returnedQty);

                    LocalDateTime lastFulfilledAt =
                            stockTxnRepo.findLastFulfilledAt(
                                    billId,
                                    bi.getItem().getId()
                            );

                    return new FulfilmentView(
                            bi.getId(),
                            bi.getItem().getItemCode(),
                            bi.getItem().getName(),

                            bi.getFulfilledQty(),
                            bi.getPendingQty(),

                            status,
                            lastFulfilledAt
                    );
                })
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        result,
                        "Fulfilments fetched"
                )
        );
    }

    /**
     * ✅ READ-SIDE STATUS DERIVATION
     * Must mirror FulfilmentService logic
     */
    private String deriveStatus(BillItem bi, BigDecimal returnedQty) {

        BigDecimal ordered = bi.getQuantity();
        BigDecimal fulfilled = bi.getFulfilledQty();
        BigDecimal fulfillable = ordered.subtract(returnedQty);

        if (returnedQty.compareTo(ordered) == 0) {
            return "RETURNED";
        }
        if (fulfilled.signum() == 0) {
            return "PENDING";
        }
        if (fulfilled.compareTo(fulfillable) < 0) {
            return "PARTIAL";
        }
        return "FULL";
    }
}
