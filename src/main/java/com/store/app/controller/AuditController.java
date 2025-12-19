package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.entity.AuditLog;
import com.store.app.repository.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogRepository auditRepo;

    public AuditController(AuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    /**
     * View audit logs for a specific entity
     * Example:
     * GET /api/audit/ITEM/{itemId}
     * GET /api/audit/BILL/{billId}
     */
    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs(
            @PathVariable String entityType,
            @PathVariable UUID entityId
    ) {
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
