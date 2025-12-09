package com.seffafbagis.api.service.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seffafbagis.api.dto.response.audit.AuditLogListResponse;
import com.seffafbagis.api.dto.response.audit.AuditLogResponse;
import com.seffafbagis.api.entity.audit.AuditLog;
import com.seffafbagis.api.enums.AuditAction;
import com.seffafbagis.api.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    // Set of sensitive keys to strictly mask
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "passwordHash", "password_hash",
            "token", "accessToken", "refreshToken", "resetToken",
            "tcKimlik", "tc_kimlik", "tcKimlikNo",
            "phone", "phoneNumber", "telefon",
            "apiKey", "secretKey", "secret",
            "creditCard", "cc", "cvv");

    @Transactional
    public void log(String action, UUID userId, String entityType, UUID entityId, Object oldValues, Object newValues) {
        try {
            String oldValuesJson = null;
            String newValuesJson = null;

            if (oldValues != null) {
                oldValuesJson = objectMapper.writeValueAsString(maskSensitiveData(oldValues));
            }
            if (newValues != null) {
                newValuesJson = objectMapper.writeValueAsString(maskSensitiveData(newValues));
            }

            String ipAddress = null;
            String userAgent = null;

            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                ipAddress = getClientIp(request);
                userAgent = request.getHeader("User-Agent");
            }

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValues(oldValuesJson)
                    .newValues(newValuesJson)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .requestId(UUID.randomUUID().toString()) // Ideally from tracing context
                    .build();

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
            // Don't throw exception to avoid breaking the main business flow
        }
    }

    public void log(AuditAction action, UUID userId, String entityType, UUID entityId, Object oldValues,
            Object newValues) {
        log(action.name(), userId, entityType, entityId, oldValues, newValues);
    }

    public void log(AuditAction action, UUID userId, String entityType, UUID entityId) {
        log(action.name(), userId, entityType, entityId, null, null);
    }

    // Compatibility method for existing Admin services
    @Transactional
    public void logAction(UUID userId, String action, String description, String entityIdStr) {
        UUID entityId = null;
        try {
            if (entityIdStr != null) {
                entityId = UUID.fromString(entityIdStr);
            }
        } catch (IllegalArgumentException e) {
            // ignore if not UUID
        }

        Map<String, String> values = new HashMap<>();
        values.put("description", description);

        log(action, userId, "ADMIN_ACTION", entityId, null, values);
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes reqAttrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (reqAttrs != null) {
                return reqAttrs.getRequest();
            }
        } catch (Exception e) {
            // Context might create issues in async threads
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @SuppressWarnings("unchecked")
    private Object maskSensitiveData(Object data) {
        if (data == null)
            return null;

        // If it's a map, mask sensitive keys
        if (data instanceof Map) {
            Map<String, Object> map = new HashMap<>((Map<String, Object>) data);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (isSensitiveKey(entry.getKey())) {
                    map.put(entry.getKey(), "[REDACTED]");
                } else if (entry.getValue() instanceof Map || entry.getValue() instanceof java.util.Collection) {
                    map.put(entry.getKey(), maskSensitiveData(entry.getValue()));
                }
            }
            return map;
        }

        // If it's a specific object type that might have sensitive data,
        // normally we'd rely on it being converted to Map or use reflection.
        // For now, assuming input is either DTO (converted to Map by ObjectMapper
        // first?)
        // or we convert it to Map for masking.
        // But here 'data' is Object passed directly.
        // Simplest way: Convert to Map via ObjectMapper, then mask, then return Map
        // (which will be serialized).

        try {
            // Avoid double serialization issues if we do this recursively.
            // Best approach: If it's a POJO, convert to Map first.
            if (!(data instanceof Map) && !(data instanceof String) && !(data instanceof Number)
                    && !(data instanceof Boolean)) {
                Map<String, Object> mapped = objectMapper.convertValue(data, Map.class);
                return maskSensitiveData(mapped);
            }
        } catch (Exception e) {
            // Fallback
        }

        return data;
    }

    private boolean isSensitiveKey(String key) {
        if (key == null)
            return false;
        String lower = key.toLowerCase();
        return SENSITIVE_KEYS.stream().anyMatch(k -> lower.contains(k.toLowerCase()));
    }

    @Transactional(readOnly = true)
    public Page<AuditLogListResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(AuditLogListResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogListResponse> getAuditLogsByUser(UUID userId, Pageable pageable) {
        return auditLogRepository.findAllByUserId(userId, pageable)
                .map(AuditLogListResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLogById(UUID id) {
        return auditLogRepository.findById(id)
                .map(AuditLogResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("Audit log not found"));
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByEntity(String entityType, UUID entityId, Pageable pageable) {
        return auditLogRepository.findAllByEntityTypeAndEntityId(entityType, entityId, pageable)
                .map(AuditLogResponse::fromEntity);
    }

    @Transactional
    public long cleanupOldLogs(int retentionDays) {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        return auditLogRepository.deleteAllByCreatedAtBefore(cutoff);
    }
}
