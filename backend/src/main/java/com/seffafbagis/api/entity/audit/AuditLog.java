package com.seffafbagis.api.entity.audit;

import com.seffafbagis.api.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Audit Log Entity.
 * 
 * Records all critical system operations for KVKK compliance and security
 * auditing.
 * Stores WHO did WHAT, WHEN, WHERE, and WHAT CHANGED.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_created_at", columnList = "created_at")
})
public class AuditLog extends BaseEntity {

    /**
     * ID of the user who performed the action.
     * Nullable for system actions or unauthenticated actions.
     */
    @Column(name = "user_id")
    private UUID userId;

    /**
     * The action performed (e.g., USER_LOGIN, PROFILE_UPDATE).
     * Stored as string to allow flexibility, typically from AuditAction enum.
     */
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    /**
     * Type of the entity affected (e.g., "User", "Campaign").
     */
    @Column(name = "entity_type", length = 100)
    private String entityType;

    /**
     * ID of the entity affected.
     */
    @Column(name = "entity_id")
    private UUID entityId;

    /**
     * Previous values of the entity/fields (JSON format).
     */
    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    /**
     * New values of the entity/fields (JSON format).
     */
    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;

    /**
     * Client IP address.
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Client user agent.
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Request Correlation ID for tracing across services.
     */
    @Column(name = "request_id", length = 64)
    private String requestId;

    /**
     * Session ID.
     */
    @Column(name = "session_id", length = 100)
    private String sessionId;

}
