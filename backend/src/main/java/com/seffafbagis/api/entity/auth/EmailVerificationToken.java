package com.seffafbagis.api.entity.auth;

import com.seffafbagis.api.entity.user.User;
import jakarta.persistence.*;
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
public class EmailVerificationToken {

    /**
     * Unique identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

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

    // ==================== CONSTRUCTORS ====================

    public EmailVerificationToken() {
    }

    public EmailVerificationToken(UUID id, User user, String tokenHash, LocalDateTime expiresAt,
            LocalDateTime verifiedAt, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
        this.createdAt = createdAt;
    }

    // ==================== GETTERS AND SETTERS ====================

    public EmailVerificationToken(User user) {
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(24);
        this.tokenHash = UUID.randomUUID().toString(); // Temporary logic
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return tokenHash; // In real impl, return raw token, store hash
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

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
