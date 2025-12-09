package com.seffafbagis.api.dto.response.audit;

import com.seffafbagis.api.entity.notification.EmailLog;
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
public class EmailLogResponse {
    private UUID id;
    private UUID userId;
    private String emailTo; // Maps to recipientEmail
    private String emailType;
    private String subject;
    private String status;
    private String provider;
    private String templateName;
    private String providerMessageId;
    private String errorMessage;
    private Integer retryCount;
    private Instant sentAt;

    public static EmailLogResponse fromEntity(EmailLog emailLog) {
        if (emailLog == null) {
            return null;
        }
        return EmailLogResponse.builder()
                .id(emailLog.getId())
                .userId(emailLog.getUserId())
                .emailTo(emailLog.getRecipientEmail())
                .emailType(emailLog.getEmailType())
                .subject(emailLog.getSubject())
                .status(emailLog.getStatus())
                .provider(emailLog.getProvider())
                .templateName(emailLog.getTemplateName())
                .providerMessageId(emailLog.getProviderMessageId())
                .errorMessage(emailLog.getErrorMessage())
                .retryCount(emailLog.getRetryCount())
                .sentAt(emailLog.getSentAt())
                .build();
    }
}
