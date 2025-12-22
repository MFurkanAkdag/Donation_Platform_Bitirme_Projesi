package com.seffafbagis.api.event;

import lombok.Getter;

import java.util.UUID;

/**
 * Event published when a campaign is rejected by admin.
 */
@Getter
public class CampaignRejectedEvent extends BaseEvent {

    private final UUID campaignId;
    private final UUID organizationId;
    private final String rejectionReason;

    public CampaignRejectedEvent(UUID triggeredBy, UUID campaignId, UUID organizationId,
            String rejectionReason) {
        super(triggeredBy);
        this.campaignId = campaignId;
        this.organizationId = organizationId;
        this.rejectionReason = rejectionReason;
    }
}
