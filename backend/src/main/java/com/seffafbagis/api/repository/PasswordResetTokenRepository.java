package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.auth.PasswordResetToken;
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
 * Repository for PasswordResetToken entity.
 * 
 * Provides database operations for password reset tokens including:
 * - Finding tokens by hash
 * - Marking tokens as used (single-use)
 * - Cleaning up expired tokens
 * - Invalidating existing unused tokens
 * 
 * @author Furkan
 * @version 1.0
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * Find password reset token by its hash.
     * 
     * @param tokenHash SHA-256 hash of the token
     * @return Optional containing the token if found
     */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * Mark all unused tokens for a user as used.
     * 
     * This is called when a new reset is requested to invalidate previous tokens.
     * 
     * @param userId User ID
     * @param now Current timestamp to set as usedAt
     * @return Number of tokens marked as used
     */
    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetToken t SET t.usedAt = :now WHERE t.user.id = :userId AND t.usedAt IS NULL")
    int markAllUnusedTokensAsUsed(@Param("userId") UUID userId, @Param("now") Instant now);

    /**
     * Delete all expired tokens.
     * 
     * Called by scheduled cleanup job (daily at 2:00 AM).
     * 
     * @param now Current timestamp for comparison
     * @return Number of tokens deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    long deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Delete all tokens for a user.
     * 
     * Used when user account is deleted.
     * 
     * @param userId User ID
     * @return Number of tokens deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.user.id = :userId")
    long deleteByUserId(@Param("userId") UUID userId);

    /**
     * Count valid (unused and not expired) tokens for a user.
     * 
     * @param userId User ID
     * @param now Current timestamp
     * @return Number of valid tokens
     */
    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.user.id = :userId AND t.usedAt IS NULL AND t.expiresAt > :now")
    long countValidTokens(@Param("userId") UUID userId, @Param("now") Instant now);
}
