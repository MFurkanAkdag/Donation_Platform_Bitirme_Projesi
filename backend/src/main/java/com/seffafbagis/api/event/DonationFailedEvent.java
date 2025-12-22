package com.seffafbagis.api.event;

import lombok.Getter;

import java.util.UUID;

/**
 * Event published when a donation fails.
 */
@Getter
public class DonationFailedEvent extends BaseEvent {

    private final UUID donationId;
    private final UUID campaignId;
    private final UUID donorId; // nullable for anonymous donations
    private final String failureReason;

    public DonationFailedEvent(UUID triggeredBy, UUID donationId, UUID campaignId,
            UUID donorId, String failureReason) {
        super(triggeredBy);
        this.donationId = donationId;
        this.campaignId = campaignId;
        this.donorId = donorId;
        this.failureReason = failureReason;
    }
}
