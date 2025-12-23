package com.seffafbagis.api.controller.audit;

import com.seffafbagis.api.dto.response.audit.AuditLogListResponse;
import com.seffafbagis.api.dto.response.audit.AuditLogResponse;
import com.seffafbagis.api.dto.response.notification.EmailLogResponse;
import com.seffafbagis.api.dto.response.audit.LoginHistoryResponse;
import com.seffafbagis.api.dto.response.common.PageResponse;
import com.seffafbagis.api.service.audit.AuditLogService;
import com.seffafbagis.api.service.audit.LoginHistoryService;
import com.seffafbagis.api.service.notification.EmailLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Audit log and history viewing (admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final LoginHistoryService loginHistoryService;
    private final EmailLogService emailLogService;

    // Audit Logs
    @GetMapping("/audit-logs")
    @Operation(summary = "Get all audit logs")
    public PageResponse<AuditLogListResponse> getAuditLogs(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.of(auditLogService.getAuditLogs(pageable));
    }

    @GetMapping("/audit-logs/{id}")
    @Operation(summary = "Get audit log details")
    public AuditLogResponse getAuditLog(@PathVariable UUID id) {
        return auditLogService.getAuditLogById(id);
    }

    @GetMapping("/audit-logs/user/{userId}")
    @Operation(summary = "Get audit logs by user")
    public PageResponse<AuditLogListResponse> getAuditLogsByUser(
            @PathVariable UUID userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.of(auditLogService.getAuditLogsByUser(userId, pageable));
    }

    @GetMapping("/audit-logs/entity/{entityType}/{entityId}")
    @Operation(summary = "Get audit logs by entity")
    public PageResponse<AuditLogResponse> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.of(auditLogService.getAuditLogsByEntity(entityType, entityId, pageable));
    }

    // Login History
    @GetMapping("/login-history/user/{userId}")
    @Operation(summary = "Get login history by user")
    public PageResponse<LoginHistoryResponse> getUserLoginHistory(
            @PathVariable UUID userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.of(loginHistoryService.getUserLoginHistory(userId, pageable));
    }

    // Email Logs
    @GetMapping("/email-logs")
    @Operation(summary = "Get all email logs")
    public PageResponse<EmailLogResponse> getEmailLogs(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.of(emailLogService.getEmailLogs(pageable));
    }

    // Statistics endpoints could be added here
}
