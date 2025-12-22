package com.seffafbagis.api.event;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event published when a donation is refunded.
 */
@Getter
public class DonationRefundedEvent extends BaseEvent {

    private final UUID donationId;
    private final UUID campaignId;
    private final BigDecimal refundAmount;
    private final String refundReason;

    public DonationRefundedEvent(UUID triggeredBy, UUID donationId, UUID campaignId,
            BigDecimal refundAmount, String refundReason) {
        super(triggeredBy);
        this.donationId = donationId;
        this.campaignId = campaignId;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
    }
}
