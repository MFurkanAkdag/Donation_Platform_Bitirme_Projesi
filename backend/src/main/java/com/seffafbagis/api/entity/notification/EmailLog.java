package com.seffafbagis.api.entity.notification;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.entity.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "email_logs")
public class EmailLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "email_to", nullable = false)
    private String emailTo;

    @Column(name = "email_type", nullable = false)
    private String emailType;

    private String provider;

    @Column(name = "template_name")
    private String templateName;

    @Column(name = "retry_count")
    private int retryCount = 0;

    @Column(nullable = false)
    private String subject;

    private String status; // 'sent', 'failed', 'bounced'

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }
}
