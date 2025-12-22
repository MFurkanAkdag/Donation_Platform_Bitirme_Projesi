package com.seffafbagis.api.event;

import lombok.Getter;

import java.util.UUID;

/**
 * Event published when an organization is verified/approved.
 */
@Getter
public class OrganizationVerifiedEvent extends BaseEvent {

    private final UUID organizationId;
    private final UUID userId;

    public OrganizationVerifiedEvent(UUID triggeredBy, UUID organizationId, UUID userId) {
        super(triggeredBy);
        this.organizationId = organizationId;
        this.userId = userId;
    }
}
