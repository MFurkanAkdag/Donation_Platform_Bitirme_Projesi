package com.seffafbagis.api.entity.auth;

import com.seffafbagis.api.entity.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Password Reset Token Entity.
 * 
 * Stores password reset tokens for users who forgot their password.
 * 
 * Security Features:
 * - Tokens are hashed before storage (SHA-256)
 * - Tokens expire after 1 hour (3600 seconds)
 * - Tokens are single-use (marked with usedAt timestamp)
 * - Each new reset request invalidates previous unused tokens
 * 
 * @author Furkan
 * @version 1.0
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_token_hash", columnList = "token_hash", unique = true),
        @Index(name = "idx_expires_at", columnList = "expires_at")
})
public class PasswordResetToken {

    /**
     * Unique identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * User who requested password reset.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    /**
     * SHA-256 hash of the token (stored for security).
     * Plain tokens are never stored in database.
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64, updatable = false)
    private String tokenHash;

    /**
     * When the token expires.
     */
    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    /**
     * When the token was used (single-use).
     * Null if not yet used.
     */
    @Column(name = "used_at")
    private Instant usedAt;

    /**
     * When the token was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ==================== CONSTRUCTORS ====================

    public PasswordResetToken() {
    }

    public PasswordResetToken(UUID id, User user, String tokenHash, Instant expiresAt, Instant usedAt,
            Instant createdAt) {
        this.id = id;
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        if (createdAt != null) {
            this.createdAt = createdAt;
        }
    }

    // ==================== GETTER & SETTER ====================

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

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Checks if token is still valid.
     * 
     * Valid = not expired AND not yet used.
     * 
     * @return true if token is valid, false otherwise
     */
    public boolean isValid() {
        return !isExpired() && !isUsed();
    }

    /**
     * Checks if token is expired.
     * 
     * @return true if token expiry time has passed
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Checks if token has been used.
     * 
     * @return true if usedAt is not null
     */
    public boolean isUsed() {
        return usedAt != null;
    }
}
