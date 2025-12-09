package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.notification.EmailLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Repository for EmailLog entity.
 * 
 * Provides database operations for email logs including:
 * - Saving email sending logs
 * - Finding logs by user
 * - Cleaning up old logs
 * 
 * @author Furkan
 * @version 1.0
 */
@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {

    /**
     * Delete old email logs (older than specified date).
     * 
     * Called by scheduled cleanup job to prevent database from growing
     * indefinitely.
     * 
     * @param before Timestamp threshold - deletes logs before this time
     * @return Number of logs deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM EmailLog l WHERE l.createdAt < :before")
    long deleteOldLogs(@Param("before") Instant before);

    /**
     * Count emails sent to a user.
     * 
     * Useful for rate limiting and tracking email sends.
     * 
     * @param userId User ID
     * @return Number of emails sent to this user
     */
    @Query("SELECT COUNT(l) FROM EmailLog l WHERE l.userId = :userId")
    long countByUserId(@Param("userId") UUID userId);

    /**
     * Count emails of specific type sent to a user.
     * 
     * @param userId    User ID
     * @param emailType Type of email (e.g., "VERIFICATION", "PASSWORD_RESET")
     * @return Number of emails of this type sent to this user
     */
    @Query("SELECT COUNT(l) FROM EmailLog l WHERE l.userId = :userId AND l.emailType = :emailType")
    long countByUserIdAndEmailType(@Param("userId") UUID userId, @Param("emailType") String emailType);

    Page<EmailLog> findAllByUserId(UUID userId, Pageable pageable);

    Page<EmailLog> findAllByEmailType(String emailType, Pageable pageable);

    Page<EmailLog> findAllByStatus(String status, Pageable pageable);
}
