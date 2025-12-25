package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.donation.PaymentSession;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.PaymentSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PaymentSession entity.
 * Handles shopping cart/payment session data access.
 * 
 * @author System
 * @version 1.0
 */
@Repository
public interface PaymentSessionRepository extends JpaRepository<PaymentSession, UUID> {

    /**
     * Find active (PENDING) payment session for a user.
     * Used to get user's current shopping cart.
     */
    Optional<PaymentSession> findByUserAndStatus(User user, PaymentSessionStatus status);

    /**
     * Find all payment sessions for a user.
     */
    List<PaymentSession> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find completed payment sessions for a user.
     */
    List<PaymentSession> findByUserAndStatusOrderByCompletedAtDesc(
            User user,
            PaymentSessionStatus status);

    /**
     * Find expired payment sessions (older than specified time and still PENDING).
     */
    @Query("SELECT ps FROM PaymentSession ps WHERE ps.status = :status AND ps.createdAt < :cutoffTime")
    List<PaymentSession> findExpiredSessions(
            @Param("status") PaymentSessionStatus status,
            @Param("cutoffTime") OffsetDateTime cutoffTime);

    /**
     * Count active sessions for a user.
     */
    long countByUserAndStatus(User user, PaymentSessionStatus status);
}
