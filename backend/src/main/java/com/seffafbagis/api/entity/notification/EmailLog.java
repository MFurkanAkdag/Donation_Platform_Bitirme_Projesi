package com.seffafbagis.api.entity.notification;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Email Log Entity.
 * 
 * Logs all emails sent by the system for audit trail and debugging.
 * 
 * Stores information about:
 * - Email recipient
 * - Email type (verification, password reset, welcome, etc.)
 * - Subject and status
 * - Send timestamp
 * - Error details if send failed
 * 
 * @author Furkan
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "email_logs", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_email_type", columnList = "email_type"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
public class EmailLog {

    /**
     * Unique identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * User ID who received the email (nullable for system emails).
     */
    @Column(name = "user_id")
    private UUID userId;

    /**
     * Recipient email address.
     */
    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    /**
     * Type of email (VERIFICATION, PASSWORD_RESET, WELCOME, PASSWORD_CHANGED,
     * etc.).
     */
    @Column(name = "email_type", nullable = false, length = 50)
    private String emailType;

    /**
     * Subject of the email.
     */
    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    /**
     * Status of email sending (SENT, FAILED, PENDING).
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status; // SENT, FAILED, PENDING

    /**
     * Error message if sending failed (nullable).
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Email service provider used (e.g., SMTP, SendGrid, AWS SES).
     */
    @Column(name = "provider", length = 50)
    private String provider;

    /**
     * Name of the template used (if applicable).
     */
    @Column(name = "template_name", length = 100)
    private String templateName;

    /**
     * Message ID returned by the provider.
     */
    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    /**
     * Number of retry attempts.
     */
    @Builder.Default
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    /**
     * When the email was actually sent.
     */
    @Column(name = "sent_at")
    private Instant sentAt;

    /**
     * When the email was sent/attempted.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    /**
     * Checks if email was sent successfully.
     * 
     * @return true if status is SENT
     */
    public boolean isSent() {
        return "SENT".equals(status);
    }

    /**
     * Checks if email sending failed.
     * 
     * @return true if status is FAILED
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }
}
