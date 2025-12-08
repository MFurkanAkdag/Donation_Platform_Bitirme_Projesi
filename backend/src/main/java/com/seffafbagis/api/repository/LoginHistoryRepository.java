package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.auth.LoginHistory;
import com.seffafbagis.api.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repository for LoginHistory entities.
 * 
 * Manages database operations for login attempt records used for security
 * monitoring, audit trails, and user activity tracking.
 * 
 * @author Furkan
 * @version 1.0
 */
@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, UUID> {

    /**
     * Finds all login attempts for a user.
     * 
     * @param user User entity
     * @param pageable Pagination parameters
     * @return Page of login history records
     */
    Page<LoginHistory> findAllByUser(User user, Pageable pageable);

    /**
     * Finds all login attempts for a user, ordered by creation date descending.
     * 
     * @param user User entity
     * @param pageable Pagination parameters
     * @return Page of login history records (newest first)
     */
    Page<LoginHistory> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Finds login attempts with specific status for a user.
     * 
     * @param user User entity
     * @param loginStatus Login status ('success', 'failed', 'blocked')
     * @param pageable Pagination parameters
     * @return Page of login history records with specified status
     */
    Page<LoginHistory> findAllByUserAndLoginStatus(User user, String loginStatus, Pageable pageable);

    /**
     * Counts login attempts with specific status after a given time.
     * Useful for detecting brute force attacks.
     * 
     * @param user User entity
     * @param loginStatus Login status to count
     * @param after Timestamp threshold
     * @return Number of matching login attempts
     */
    long countByUserAndLoginStatusAndCreatedAtAfter(User user, String loginStatus, LocalDateTime after);

    /**
     * Deletes old login history records.
     * Useful for cleanup jobs to remove old history.
     * 
     * @param dateTime Timestamp threshold
     * @return Number of records deleted
     */
    @Modifying
    @Query("DELETE FROM LoginHistory h WHERE h.createdAt < :dateTime")
    int deleteAllByCreatedAtBefore(@Param("dateTime") LocalDateTime dateTime);
}
