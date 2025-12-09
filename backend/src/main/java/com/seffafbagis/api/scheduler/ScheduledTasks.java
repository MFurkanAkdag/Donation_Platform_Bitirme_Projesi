package com.seffafbagis.api.scheduler;

import com.seffafbagis.api.service.audit.AuditLogService;
import com.seffafbagis.api.service.audit.LoginHistoryService;
import com.seffafbagis.api.service.notification.EmailLogService;
// import com.seffafbagis.api.service.system.SystemSettingService; // Uncomment when available
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduledTasks {

    private final AuditLogService auditLogService;
    private final LoginHistoryService loginHistoryService;
    private final EmailLogService emailLogService;
    // private final SystemSettingService systemSettingService;

    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void cleanupOldAuditLogs() {
        log.info("Starting audit log cleanup...");
        try {
            // int retentionDays =
            // systemSettingService.getSettingValueOrDefault("audit_log_retention_days",
            // 365);
            int retentionDays = 365; // Default for now
            long deletedCount = auditLogService.cleanupOldLogs(retentionDays);
            log.info("Audit log cleanup completed. Deleted {} old logs.", deletedCount);
        } catch (Exception e) {
            log.error("Audit log cleanup failed", e);
        }
    }

    @Scheduled(cron = "0 0 3 * * ?") // Daily at 3 AM
    public void cleanupOldLoginHistory() {
        log.info("Starting login history cleanup...");
        try {
            // int retentionDays =
            // systemSettingService.getSettingValueOrDefault("login_history_retention_days",
            // 90);
            int retentionDays = 90; // Default for now
            long deletedCount = loginHistoryService.cleanupOldHistory(retentionDays);
            log.info("Login history cleanup completed. Deleted {} old records.", deletedCount);
        } catch (Exception e) {
            log.error("Login history cleanup failed", e);
        }
    }

    @Scheduled(cron = "0 0 4 * * ?") // Daily at 4 AM
    public void cleanupOldEmailLogs() {
        log.info("Starting email log cleanup...");
        try {
            // int retentionDays =
            // systemSettingService.getSettingValueOrDefault("email_log_retention_days",
            // 180);
            int retentionDays = 180; // Default 6 months for now
            long deletedCount = emailLogService.cleanupOldLogs(retentionDays);
            log.info("Email log cleanup completed. Deleted {} old records.", deletedCount);
        } catch (Exception e) {
            log.error("Email log cleanup failed", e);
        }
    }
}
