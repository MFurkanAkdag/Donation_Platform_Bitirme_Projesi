package com.seffafbagis.api.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abstract base class for all domain events in the system.
 * Provides common fields for event tracking and auditing.
 */
@Getter
public abstract class BaseEvent {

    /**
     * Unique identifier for this event instance
     */
    private final UUID eventId;

    /**
     * Timestamp when the event occurred
     */
    private final LocalDateTime occurredAt;

    /**
     * User ID who triggered the event (can be null for system actions)
     */
    private final UUID triggeredBy;

    protected BaseEvent(UUID triggeredBy) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = LocalDateTime.now();
        this.triggeredBy = triggeredBy;
    }
}
