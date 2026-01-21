package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.BillItemResponse;
import com.store.app.entity.BillItem;
import com.store.app.entity.User;
import com.store.app.repository.BillItemRepository;
import com.store.app.repository.ReturnItemRepository;
import com.store.app.service.AuthService;
import com.store.app.service.BillItemService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bill-items")
@RequiredArgsConstructor
public class BillItemController {

    private final BillItemService billItemService;
    private final BillItemRepository billItemRepo;
    private final ReturnItemRepository returnItemRepo;
    private final AuthService authService;

    /* =====================================================
       🧾 GET SINGLE BILL ITEM (RECONCILED)
       ===================================================== */

    @GetMapping("/{billItemId}")
    public ResponseEntity<ApiResponse<BillItemResponse>> getBillItem(
            @PathVariable UUID billItemId,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireBillingOrOwner(user);

        BillItem bi = billItemService.getBillItem(billItemId);

        BigDecimal returnedQty =
                returnItemRepo.getTotalReturnedQtyForBillItem(bi.getId());

        if (returnedQty == null) {
            returnedQty = BigDecimal.ZERO;
        }

        BigDecimal netQty = bi.getQuantity().subtract(returnedQty);
        BigDecimal fulfillableQty = netQty;

        String status;
        if (returnedQty.compareTo(bi.getQuantity()) == 0) {
            status = "RETURNED";
        } else if (bi.getFulfilledQty().signum() == 0) {
            status = "PENDING";
        } else if (bi.getFulfilledQty().compareTo(fulfillableQty) < 0) {
            status = "PARTIAL";
        } else {
            status = "FULL";
        }

        BillItemResponse response = new BillItemResponse(
                bi.getId(),
                bi.getItem().getItemCode(),
                bi.getItem().getName(),

                bi.getQuantity(),
                bi.getFulfilledQty(),
                bi.getPendingQty(),

                returnedQty,
                netQty,

                bi.getPrice(),
                bi.getAmount(),
                status
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, response, "Bill item loaded")
        );
    }

    /* =====================================================
       📦 GET BILL ITEMS (RECONCILED VIEW)
       ===================================================== */

    @GetMapping("/{billId}/items")
    public ResponseEntity<ApiResponse<List<BillItemResponse>>> getBillItems(
            @PathVariable UUID billId,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        List<BillItem> items =
                billItemRepo.findByBillIdOrderByIdAsc(billId);

        List<BillItemResponse> response = items.stream()
                .map(bi -> {

                    BigDecimal returnedQty =
                            returnItemRepo.getTotalReturnedQtyForBillItem(bi.getId());

                    if (returnedQty == null) {
                        returnedQty = BigDecimal.ZERO;
                    }

                    BigDecimal netQty =
                            bi.getQuantity().subtract(returnedQty);

                    BigDecimal fulfillableQty = netQty;

                    String status;
                    if (returnedQty.compareTo(bi.getQuantity()) == 0) {
                        status = "RETURNED";
                    } else if (bi.getFulfilledQty().signum() == 0) {
                        status = "PENDING";
                    } else if (bi.getFulfilledQty().compareTo(fulfillableQty) < 0) {
                        status = "PARTIAL";
                    } else {
                        status = "FULL";
                    }

                    return new BillItemResponse(
                            bi.getId(),
                            bi.getItem().getItemCode(),
                            bi.getItem().getName(),

                            bi.getQuantity(),
                            bi.getFulfilledQty(),
                            bi.getPendingQty(),

                            returnedQty,
                            netQty,

                            bi.getPrice(),
                            bi.getAmount(),
                            status
                    );
                })
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(true, response, "Bill items fetched")
        );
    }
}
