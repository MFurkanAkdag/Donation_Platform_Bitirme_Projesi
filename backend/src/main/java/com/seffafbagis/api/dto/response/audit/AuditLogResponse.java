package com.seffafbagis.api.dto.response.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seffafbagis.api.entity.audit.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String action;
    private String entityType;
    private UUID entityId;
    private Map<String, Object> oldValues;
    private Map<String, Object> newValues;
    private String ipAddress;
    private String userAgent;
    private String requestId;
    private String sessionId;
    private Instant createdAt;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public static AuditLogResponse fromEntity(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }

        Map<String, Object> oldValuesMap = null;
        Map<String, Object> newValuesMap = null;

        try {
            if (auditLog.getOldValues() != null) {
                oldValuesMap = objectMapper.readValue(auditLog.getOldValues(), Map.class);
            }
            if (auditLog.getNewValues() != null) {
                newValuesMap = objectMapper.readValue(auditLog.getNewValues(), Map.class);
            }
        } catch (JsonProcessingException e) {
            // Log error or ignore, return empty map?
            // For DTO mapping, ignoring is safer than crashing
        }

        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                // userEmail is not directly in AuditLog, must be populated by Service
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .oldValues(oldValuesMap)
                .newValues(newValuesMap)
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .requestId(auditLog.getRequestId())
                .sessionId(auditLog.getSessionId())
                .createdAt(auditLog.getCreatedAt() != null ? auditLog.getCreatedAt().toInstant() : null)
                .build();
    }
}
