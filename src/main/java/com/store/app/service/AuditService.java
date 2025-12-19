package com.store.app.service;

import com.store.app.entity.AuditLog;
import com.store.app.entity.User;
import com.store.app.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditRepo;

    public AuditService(AuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    public void log(
            String entityType,
            UUID entityId,
            String action,
            String oldValue,
            String newValue,
            User user
    ) {
        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setPerformedBy(user);

        auditRepo.save(log);
    }
}
