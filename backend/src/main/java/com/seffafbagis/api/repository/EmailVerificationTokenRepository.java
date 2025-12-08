package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.auth.EmailVerificationToken;
import com.seffafbagis.api.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
     * Deletes expired verification tokens.
     * Useful for cleanup jobs to remove old expired tokens.
     * 
     * @param dateTime Timestamp threshold
     * @return Number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :dateTime")
    int deleteAllByExpiresAtBefore(@Param("dateTime") LocalDateTime dateTime);
}
