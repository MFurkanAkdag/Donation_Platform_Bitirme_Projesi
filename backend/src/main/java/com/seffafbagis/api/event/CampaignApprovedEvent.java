package com.seffafbagis.api.event;

import lombok.Getter;

import java.util.UUID;

/**
 * Event published when a campaign is approved by admin.
 */
@Getter
public class CampaignApprovedEvent extends BaseEvent {

    private final UUID campaignId;
    private final UUID organizationId;
    private final UUID approvedBy;

    public CampaignApprovedEvent(UUID triggeredBy, UUID campaignId, UUID organizationId,
            UUID approvedBy) {
        super(triggeredBy);
        this.campaignId = campaignId;
        this.organizationId = organizationId;
        this.approvedBy = approvedBy;
    }
}
