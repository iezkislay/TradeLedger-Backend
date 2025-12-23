package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.DashboardResponse;
import com.store.app.dto.DashboardKpiResponse;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthService authService;

    /**
     * OLD ENDPOINT (UNCHANGED)
     * Used for full dashboard data
     * Access: OWNER only
     */
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard(
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        dashboardService.getDashboard(),
                        "Dashboard loaded"
                )
        );
    }

    /**
     * NEW ENDPOINT (ADDED)
     * Used for KPI widgets
     * Access: OWNER only (kept consistent with dashboard)
     */
    @GetMapping("/kpis")
    public ResponseEntity<ApiResponse<DashboardKpiResponse>> getKpis(
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        dashboardService.getKpis(),
                        "Dashboard KPIs loaded"
                )
        );
    }
}
