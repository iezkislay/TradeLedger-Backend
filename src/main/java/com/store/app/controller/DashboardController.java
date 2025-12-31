package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.DashboardResponse;
import com.store.app.dto.DashboardKpiResponse;
import com.store.app.dto.DashboardSummaryResponse;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import com.store.app.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthService authService;

    /**
     * =========================
     * FULL DASHBOARD (LEGACY)
     * =========================
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
     * =========================
     * KPI DASHBOARD (WIDGETS)
     * =========================
     * Access: OWNER only
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

    /**
     * =========================
     * DASHBOARD SUMMARY (RANGE)
     * =========================
     * Example:
     * /api/dashboard/summary?from=2025-03-01&to=2025-03-31
     * Access: OWNER only
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> summary(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        authService.requireOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        dashboardService.getDashboardSummary(from, to),
                        "Dashboard summary loaded"
                )
        );
    }
}
