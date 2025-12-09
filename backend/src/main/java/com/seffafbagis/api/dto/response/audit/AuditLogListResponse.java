package com.seffafbagis.api.dto.response.audit;

import com.seffafbagis.api.entity.audit.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogListResponse {
    private UUID id;
    private String userEmail; // Need to fetch user to get email, or store in entity?
    // Optimization: For now, we might leave email null or fetch it in Service.
    // The entity stores userId.
    private String action;
    private String entityType;
    private UUID entityId;
    private String ipAddress;
    private Instant createdAt;

    public static AuditLogListResponse fromEntity(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }
        return AuditLogListResponse.builder()
                .id(auditLog.getId())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .ipAddress(auditLog.getIpAddress())
                .createdAt(auditLog.getCreatedAt() != null ? auditLog.getCreatedAt().toInstant() : null)
                .build();
    }
}
