package com.seffafbagis.api.event;

import lombok.Getter;

import java.util.UUID;

/**
 * Event fired when a campaign is completed.
 */
@Getter
public class CampaignCompletedEvent extends BaseEvent {

    private final UUID campaignId;
    private final UUID organizationId;
    private final String campaignTitle;

    public CampaignCompletedEvent(UUID triggeredBy, UUID campaignId,
            UUID organizationId, String campaignTitle) {
        super(triggeredBy);
        this.campaignId = campaignId;
        this.organizationId = organizationId;
        this.campaignTitle = campaignTitle;
    }
}
