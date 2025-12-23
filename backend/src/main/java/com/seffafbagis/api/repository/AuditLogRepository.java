package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.audit.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Repository for AuditLog entity.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<AuditLog> findAllByUserId(UUID userId, Pageable pageable);

    Page<AuditLog> findAllByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    Page<AuditLog> findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, UUID entityId,
            Pageable pageable);

    Page<AuditLog> findAllByEntityTypeAndEntityId(String entityType, UUID entityId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM AuditLog a WHERE a.createdAt < :before")
    int deleteOldLogs(@Param("before") OffsetDateTime before);

    @Modifying
    @Transactional
    long deleteAllByCreatedAtBefore(Instant before);
}
