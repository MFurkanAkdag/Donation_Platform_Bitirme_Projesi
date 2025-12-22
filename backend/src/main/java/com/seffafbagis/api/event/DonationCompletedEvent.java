package com.seffafbagis.api.event;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event published when a donation is successfully completed.
 */
@Getter
public class DonationCompletedEvent extends BaseEvent {

    private final UUID donationId;
    private final UUID campaignId;
    private final UUID organizationId;
    private final UUID donorId; // nullable for anonymous donations
    private final BigDecimal amount;
    private final UUID transactionId;

    public DonationCompletedEvent(UUID triggeredBy, UUID donationId, UUID campaignId,
            UUID organizationId, UUID donorId, BigDecimal amount, UUID transactionId) {
        super(triggeredBy);
        this.donationId = donationId;
        this.campaignId = campaignId;
        this.organizationId = organizationId;
        this.donorId = donorId;
        this.amount = amount;
        this.transactionId = transactionId;
    }
}
