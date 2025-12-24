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
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for LoginHistory entity.
 */
@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, UUID> {

    Page<LoginHistory> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<LoginHistory> findAllByUser(User user, Pageable pageable);

    Page<LoginHistory> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Optional<LoginHistory> findTopByUserOrderByCreatedAtDesc(User user);

    long countByUserId(UUID userId);

    long countByUserAndLoginStatusAndCreatedAtAfter(User user, String loginStatus, OffsetDateTime createdAt);

    @Query("SELECT COUNT(h) FROM LoginHistory h WHERE h.user.id = :userId AND h.loginStatus = 'failed' AND h.createdAt > :since")
    long countFailedLoginsSince(@Param("userId") UUID userId, @Param("since") OffsetDateTime since);

    @Query("SELECT COUNT(DISTINCT h.user.id) FROM LoginHistory h WHERE h.createdAt > :since")
    long countDistinctUserIdByCreatedAtAfter(@Param("since") OffsetDateTime since);

    @Modifying
    @Transactional
    @Query("DELETE FROM LoginHistory h WHERE h.createdAt < :before")
    int deleteOldHistory(@Param("before") OffsetDateTime before);

    @Modifying
    @Transactional
    long deleteAllByCreatedAtBefore(OffsetDateTime before);
}
