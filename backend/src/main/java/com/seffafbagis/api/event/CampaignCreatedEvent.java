package com.seffafbagis.api.event;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event published when a new campaign is created.
 */
@Getter
public class CampaignCreatedEvent extends BaseEvent {

    private final UUID campaignId;
    private final UUID organizationId;
    private final String campaignTitle;
    private final BigDecimal targetAmount;

    public CampaignCreatedEvent(UUID triggeredBy, UUID campaignId, UUID organizationId,
            String campaignTitle, BigDecimal targetAmount) {
        super(triggeredBy);
        this.campaignId = campaignId;
        this.organizationId = organizationId;
        this.campaignTitle = campaignTitle;
        this.targetAmount = targetAmount;
    }
}
