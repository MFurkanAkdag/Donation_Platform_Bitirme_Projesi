package com.seffafbagis.api.event;

import com.seffafbagis.api.enums.CampaignStatus;
import lombok.Getter;

import java.util.UUID;

/**
 * Event published when a campaign status changes.
 */
@Getter
public class CampaignStatusChangedEvent extends BaseEvent {

    private final UUID campaignId;
    private final CampaignStatus previousStatus;
    private final CampaignStatus newStatus;

    public CampaignStatusChangedEvent(UUID triggeredBy, UUID campaignId,
            CampaignStatus previousStatus, CampaignStatus newStatus) {
        super(triggeredBy);
        this.campaignId = campaignId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }
}
