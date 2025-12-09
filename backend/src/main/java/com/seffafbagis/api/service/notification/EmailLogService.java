package com.seffafbagis.api.service.notification;

import com.seffafbagis.api.dto.response.audit.EmailLogResponse;
import com.seffafbagis.api.entity.notification.EmailLog;
import com.seffafbagis.api.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailLogService {

    private final EmailLogRepository emailLogRepository;

    @Transactional
    public void logEmailSent(UUID userId, String emailTo, String emailType, String subject, String provider,
            String templateName, String messageId) {
        try {
            EmailLog log = EmailLog.builder()
                    .userId(userId)
                    .recipientEmail(emailTo)
                    .emailType(emailType)
                    .subject(subject)
                    .status("SENT")
                    .provider(provider)
                    .templateName(templateName)
                    .providerMessageId(messageId)
                    .sentAt(Instant.now())
                    .build();
            emailLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to log email sent: {}", e.getMessage());
        }
    }

    @Transactional
    public void logEmailFailed(UUID userId, String emailTo, String emailType, String subject, String errorMessage,
            int retryCount) {
        try {
            EmailLog log = EmailLog.builder()
                    .userId(userId)
                    .recipientEmail(emailTo)
                    .emailType(emailType)
                    .subject(subject)
                    .status("FAILED")
                    .errorMessage(errorMessage)
                    .retryCount(retryCount)
                    .build();
            emailLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to log email failure: {}", e.getMessage());
        }
    }

    @Transactional
    public void updateEmailStatus(UUID logId, String status, String providerMessageId) {
        emailLogRepository.findById(logId).ifPresent(log -> {
            log.setStatus(status);
            if (providerMessageId != null) {
                log.setProviderMessageId(providerMessageId);
            }
            emailLogRepository.save(log);
        });
    }

    @Transactional(readOnly = true)
    public Page<EmailLogResponse> getEmailLogs(Pageable pageable) {
        return emailLogRepository.findAll(pageable)
                .map(EmailLogResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<EmailLogResponse> getEmailLogsByUser(UUID userId, Pageable pageable) {
        return emailLogRepository.findAllByUserId(userId, pageable)
                .map(EmailLogResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<EmailLogResponse> getEmailLogsByType(String emailType, Pageable pageable) {
        return emailLogRepository.findAllByEmailType(emailType, pageable)
                .map(EmailLogResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<EmailLogResponse> getEmailLogsByStatus(String status, Pageable pageable) {
        return emailLogRepository.findAllByStatus(status, pageable)
                .map(EmailLogResponse::fromEntity);
    }

    /**
     * Cleanup old email logs based on retention days.
     * 
     * @param retentionDays Number of days to keep logs
     * @return Number of deleted records
     */
    @Transactional
    public long cleanupOldLogs(int retentionDays) {
        try {
            Instant cutoff = Instant.now().minus(retentionDays, java.time.temporal.ChronoUnit.DAYS);
            return emailLogRepository.deleteOldLogs(cutoff);
        } catch (Exception e) {
            log.error("Failed to cleanup email logs: {}", e.getMessage());
            return 0;
        }
    }
}
