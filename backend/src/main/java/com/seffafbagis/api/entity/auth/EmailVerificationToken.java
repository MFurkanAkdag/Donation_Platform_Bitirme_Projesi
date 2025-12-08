package com.seffafbagis.api.entity.auth;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Email verification token entity for account email verification process.
 * 
 * Stores tokens used for verifying user email addresses during registration
 * and email change operations. Tokens expire after a configurable period.
 * 
 * @author Furkan
 * @version 1.0
 */
@Entity
@Table(name = "email_verification_tokens")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationToken extends BaseEntity {

    /**
     * User associated with this verification token.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Hash of the verification token.
     * Stores hash instead of actual token for security.
     * Never store plaintext tokens in database.
     */
    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    /**
     * Timestamp when this token expires.
     * User must verify email before this time.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Timestamp when user verified their email using this token.
     * Null until verification is completed.
     */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    /**
     * Timestamp when this token was created.
     * Automatically set by database.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ==================== METHODS ====================

    /**
     * Checks if this token has expired.
     * 
     * @return true if current time is after expiresAt
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Checks if email has been verified with this token.
     * 
     * @return true if verifiedAt is not null
     */
    public boolean isVerified() {
        return verifiedAt != null;
    }

    /**
     * Checks if this token is still valid for verification.
     * Token must not be expired and not already verified.
     * 
     * @return true if token can still be used
     */
    public boolean isValid() {
        return !isExpired() && !isVerified();
    }

    /**
     * Marks email as verified using this token.
     * Sets verifiedAt to current timestamp.
     */
    public void markAsVerified() {
        this.verifiedAt = LocalDateTime.now();
    }
}
