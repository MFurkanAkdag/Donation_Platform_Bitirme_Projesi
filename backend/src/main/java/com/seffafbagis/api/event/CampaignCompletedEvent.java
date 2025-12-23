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
    private final java.math.BigDecimal collectedAmount;
    private final Integer donorCount;
    private final java.time.LocalDateTime evidenceDeadline;

    public CampaignCompletedEvent(UUID triggeredBy, UUID campaignId,
            UUID organizationId, java.math.BigDecimal collectedAmount,
            Integer donorCount, java.time.LocalDateTime evidenceDeadline) {
        super(triggeredBy);
        this.campaignId = campaignId;
        this.organizationId = organizationId;
        this.campaignTitle = null; // Removed from constructor to match service usage, or we can fetch it but
                                   // service doesn't pass it.
        this.collectedAmount = collectedAmount;
        this.donorCount = donorCount;
        this.evidenceDeadline = evidenceDeadline;
    }
}
