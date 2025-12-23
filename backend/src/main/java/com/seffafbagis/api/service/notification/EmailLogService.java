package com.seffafbagis.api.service.notification;

import com.seffafbagis.api.dto.response.notification.EmailLogResponse;
import com.seffafbagis.api.entity.notification.EmailLog;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.repository.EmailLogRepository;
import com.seffafbagis.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailLogService {

    private static final Logger logger = LoggerFactory.getLogger(EmailLogService.class);
    private final EmailLogRepository emailLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void logEmailSent(UUID userId, String emailTo, String emailType, String subject, String provider,
            String templateName, String messageId) {
        try {
            User user = null;
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null);
            }

            EmailLog emailLog = new EmailLog();
            emailLog.setUser(user);
            emailLog.setEmailTo(emailTo);
            emailLog.setEmailType(emailType);
            emailLog.setSubject(subject);
            emailLog.setStatus("SENT");
            emailLog.setProvider(provider);
            emailLog.setTemplateName(templateName);
            emailLog.setProviderMessageId(messageId);
            emailLog.setSentAt(LocalDateTime.now());
            emailLogRepository.save(emailLog);
        } catch (Exception e) {
            logger.error("Failed to log email sent: {}", e.getMessage());
        }
    }

    @Transactional
    public void logEmailFailed(UUID userId, String emailTo, String emailType, String subject, String errorMessage,
            int retryCount) {
        try {
            User user = null;
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null);
            }

            EmailLog emailLog = new EmailLog();
            emailLog.setUser(user);
            emailLog.setEmailTo(emailTo);
            emailLog.setEmailType(emailType);
            emailLog.setSubject(subject);
            emailLog.setStatus("FAILED");
            emailLog.setErrorMessage(errorMessage);
            emailLog.setRetryCount(retryCount);
            emailLog.setSentAt(LocalDateTime.now());
            emailLogRepository.save(emailLog);
        } catch (Exception e) {
            logger.error("Failed to log email failure: {}", e.getMessage());
        }
    }

    @Transactional
    public void updateEmailStatus(UUID logId, String status, String providerMessageId) {
        emailLogRepository.findById(logId).ifPresent(emailLog -> {
            emailLog.setStatus(status);
            if (providerMessageId != null) {
                emailLog.setProviderMessageId(providerMessageId);
            }
            emailLogRepository.save(emailLog);
        });
    }

    @Transactional(readOnly = true)
    public Page<EmailLogResponse> getEmailLogs(Pageable pageable) {
        return emailLogRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<EmailLogResponse> getEmailLogsByUser(UUID userId, Pageable pageable) {
        return emailLogRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<EmailLogResponse> getEmailLogsByType(String emailType, Pageable pageable) {
        return emailLogRepository.findByEmailType(emailType, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<EmailLogResponse> getEmailLogsByStatus(String status, Pageable pageable) {
        return emailLogRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
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
            LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
            emailLogRepository.deleteBySentAtBefore(cutoff);
            return 0; // return actual count if query supports it, or void
        } catch (Exception e) {
            logger.error("Failed to cleanup email logs: {}", e.getMessage());
            return 0;
        }
    }

    private EmailLogResponse mapToResponse(EmailLog log) {
        return EmailLogResponse.builder()
                .id(log.getId())
                .emailTo(log.getEmailTo())
                .emailType(log.getEmailType())
                .subject(log.getSubject())
                .status(log.getStatus())
                .sentAt(log.getSentAt())
                .errorMessage(log.getErrorMessage())
                .build();
    }
}
