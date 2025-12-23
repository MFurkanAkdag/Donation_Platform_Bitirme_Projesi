package com.seffafbagis.api.event;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event fired when a new donation is created.
 */
@Getter
public class DonationCreatedEvent extends BaseEvent {

    private final UUID donationId;
    private final UUID campaignId;
    private final UUID donorId;
    private final BigDecimal amount;
    private final String currency;
    private final Boolean isAnonymous;

    public DonationCreatedEvent(UUID triggeredBy, UUID donationId, UUID campaignId,
            UUID donorId, BigDecimal amount, String currency,
            Boolean isAnonymous) {
        super(triggeredBy);
        this.donationId = donationId;
        this.campaignId = campaignId;
        this.donorId = donorId;
        this.amount = amount;
        this.currency = currency;
        this.isAnonymous = isAnonymous;
    }
}
