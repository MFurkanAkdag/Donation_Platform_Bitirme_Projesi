package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for RefreshToken entity.
 * 
 * Manages refresh tokens used for obtaining new JWT access tokens.
 * 
 * @author Furkan
 * @version 1.0
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Find refresh token by token string.
     * 
     * @param token Token string
     * @return Optional containing the token if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find refresh token by token hash (SHA-256).
     * 
     * @param tokenHash Hashed token
     * @return Optional containing the token if found
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Delete all refresh tokens for a user.
     * 
     * Used when user logs out from all devices or changes password.
     * 
     * @param userId User ID
     * @return Number of tokens deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken t WHERE t.user.id = :userId")
    int deleteByUserId(@Param("userId") UUID userId);

    /**
     * Delete expired refresh tokens.
     * 
     * Called by scheduled cleanup job.
     * 
     * @param now Current timestamp
     * @return Number of tokens deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken t WHERE t.expiresAt < :now")
    long deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Find refresh token by user ID and device fingerprint.
     * 
     * @param userId User ID
     * @param deviceFingerprint Device fingerprint
     * @return Optional containing the token if found
     */
    Optional<RefreshToken> findByUserIdAndDeviceFingerprint(@Param("userId") UUID userId, @Param("deviceFingerprint") String deviceFingerprint);
}
