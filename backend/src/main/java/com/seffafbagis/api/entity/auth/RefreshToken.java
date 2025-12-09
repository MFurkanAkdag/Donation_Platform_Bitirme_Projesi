package com.seffafbagis.api.entity.auth;

import com.seffafbagis.api.entity.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Refresh Token Entity.
 * 
 * Stores refresh tokens used to obtain new JWT access tokens.
 * 
 * Security Features:
 * - Tokens are hashed before storage (SHA-256)
 * - Tokens expire after 7 days (604800 seconds)
 * - Each device/browser gets unique token
 * - Single-use tokens (invalidated after use for rotation)
 * - Device fingerprinting for additional security
 * 
 * @author Furkan
 * @version 1.0
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_token_hash", columnList = "token_hash", unique = true),
        @Index(name = "idx_expires_at", columnList = "expires_at"),
        @Index(name = "idx_user_device", columnList = "user_id,device_fingerprint")
})
public class RefreshToken {

    /**
     * Unique identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * User who owns this token.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    /**
     * Plain token (returned to client).
     * Stored only in memory during response, not persisted.
     */
    @Transient
    private String token;

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
     * Device fingerprint to bind token to specific device.
     * Helps prevent token theft if intercepted.
     */
    @Column(name = "device_fingerprint", length = 255)
    private String deviceFingerprint;

    /**
     * Device name/description (optional).
     * E.g., "Chrome on Windows", "Safari on iPhone"
     */
    @Column(name = "device_name", length = 255)
    private String deviceName;

    /**
     * IP address where token was issued.
     */
    @Column(name = "issued_ip_address", length = 45) // IPv6 max length
    private String issuedIpAddress;

    /**
     * When the token was created/issued.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Last time this token was used to get new access token.
     */
    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    /**
     * When the token was revoked (if revoked manually).
     * Null if still active.
     */
    @Column(name = "revoked_at")
    private Instant revokedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // ==================== CONSTRUCTORS ====================

    public RefreshToken() {
    }

    public RefreshToken(UUID id, User user, String token, String tokenHash, Instant expiresAt, String deviceFingerprint,
            String deviceName, String issuedIpAddress, Instant createdAt, Instant lastUsedAt, Instant revokedAt) {
        this.id = id;
        this.user = user;
        this.token = token;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.deviceFingerprint = deviceFingerprint;
        this.deviceName = deviceName;
        this.issuedIpAddress = issuedIpAddress;
        this.createdAt = createdAt;
        this.lastUsedAt = lastUsedAt;
        this.revokedAt = revokedAt;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getIssuedIpAddress() {
        return issuedIpAddress;
    }

    public void setIssuedIpAddress(String issuedIpAddress) {
        this.issuedIpAddress = issuedIpAddress;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    /**
     * Checks if token is still valid and active.
     * 
     * Valid = not expired AND not revoked.
     * 
     * @return true if token is valid
     */
    public boolean isValid() {
        return !isExpired() && !isRevoked();
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
     * Checks if token has been revoked.
     * 
     * @return true if revokedAt is not null
     */
    public boolean isRevoked() {
        return revokedAt != null;
    }
}
