package com.seffafbagis.api.event;

import lombok.Getter;

import java.util.UUID;

/**
 * Event published when an organization application is rejected.
 */
@Getter
public class OrganizationRejectedEvent extends BaseEvent {

    private final UUID organizationId;
    private final String rejectionReason;

    public OrganizationRejectedEvent(UUID triggeredBy, UUID organizationId, String rejectionReason) {
        super(triggeredBy);
        this.organizationId = organizationId;
        this.rejectionReason = rejectionReason;
    }
}
