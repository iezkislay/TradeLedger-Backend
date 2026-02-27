package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.CreateWorkOrderRequest;
import com.store.app.dto.WorkOrderSummaryResponse;
import com.store.app.entity.User;
import com.store.app.entity.WorkOrder;
import com.store.app.service.AuthService;
import com.store.app.service.WorkOrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final AuthService authService;

    public WorkOrderController(
            WorkOrderService workOrderService,
            AuthService authService
    ) {
        this.workOrderService = workOrderService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkOrder>> create(
            @RequestBody CreateWorkOrderRequest request,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        WorkOrder wo = workOrderService.createWorkOrder(request, user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        wo,
                        "Work order created"
                )
        );
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<WorkOrderSummaryResponse> getSummary(
            @PathVariable UUID id,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }
        return ResponseEntity.ok(
                workOrderService.getSummary(id)
        );
    }
}
