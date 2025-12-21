package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.entity.AuditLog;
import com.store.app.entity.User;
import com.store.app.repository.AuditLogRepository;
import com.store.app.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogRepository auditRepo;
    private final AuthService authService;

    public AuditController(
            AuditLogRepository auditRepo,
            AuthService authService
    ) {
        this.auditRepo = auditRepo;
        this.authService = authService;
    }

    /**
     * 🔍 View audit logs for a specific entity
     * OWNER only
     *
     * Examples:
     * GET /api/audit/ITEM/{itemId}
     * GET /api/audit/BILL/{billId}
     */
    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        // 🔒 OWNER only
        authService.requireOwner(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        auditRepo.findByEntityTypeAndEntityIdOrderByPerformedAtDesc(
                                entityType,
                                entityId
                        ),
                        "Audit logs fetched"
                )
        );
    }
}
