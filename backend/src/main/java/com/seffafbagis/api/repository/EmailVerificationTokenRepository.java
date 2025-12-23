package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.auth.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for EmailVerificationToken entity.
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    Optional<EmailVerificationToken> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") OffsetDateTime now);

    @Query("SELECT COUNT(t) FROM EmailVerificationToken t WHERE t.user.id = :userId AND t.createdAt > :since")
    long countRecentTokens(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    @Modifying
    @Transactional
    @Query("UPDATE EmailVerificationToken t SET t.verifiedAt = :now WHERE t.user.id = :userId AND t.verifiedAt IS NULL")
    int markAllUnverifiedTokensAsVerified(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
}
