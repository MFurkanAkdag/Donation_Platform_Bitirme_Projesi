package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.auth.EmailVerificationToken;
import com.seffafbagis.api.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for EmailVerificationToken entities.
 * 
 * Manages database operations for email verification tokens used during
 * user registration and email change workflows.
 * 
 * @author Furkan
 * @version 1.0
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    /**
     * Finds a token by its hash.
     * 
     * @param tokenHash Hash of the verification token
     * @return Token if found
     */
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    /**
     * Finds the pending (unverified) verification token for a user.
     * 
     * @param user User entity
     * @return Unverified token if exists
     */
    Optional<EmailVerificationToken> findByUserAndVerifiedAtIsNull(User user);

    /**
     * Count recent verification tokens created for rate limiting.
     * 
     * Used to enforce rate limiting on resend (max 3 per hour).
     * 
     * @param userId User ID
     * @param since Timestamp to check from (e.g., 1 hour ago)
     * @return Number of tokens created since the given time
     */
    @Query("SELECT COUNT(t) FROM EmailVerificationToken t WHERE t.user.id = :userId AND t.createdAt > :since")
    long countRecentTokens(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    /**
     * Mark all unverified tokens for a user as verified.
     * 
     * Called when one token is successfully used to prevent
     * other old tokens from being reused.
     * 
     * @param userId User ID
     * @param now Current timestamp to set as verifiedAt
     * @return Number of tokens marked as verified
     */
    @Modifying
    @Transactional
    @Query("UPDATE EmailVerificationToken t SET t.verifiedAt = :now WHERE t.user.id = :userId AND t.verifiedAt IS NULL")
    int markAllUnverifiedTokensAsVerified(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    /**
     * Deletes expired verification tokens.
     * Useful for cleanup jobs to remove old expired tokens.
     * 
     * @param now Current timestamp for comparison
     * @return Number of tokens deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :now")
    long deleteExpiredTokens(@Param("now") LocalDateTime now);

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
    @Query("DELETE FROM EmailVerificationToken t WHERE t.user.id = :userId")
    long deleteByUserId(@Param("userId") UUID userId);
}
