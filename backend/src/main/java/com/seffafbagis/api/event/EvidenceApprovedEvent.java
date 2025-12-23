package com.seffafbagis.api.event;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event fired when evidence is approved.
 */
@Getter
public class EvidenceApprovedEvent extends BaseEvent {

    private final UUID evidenceId;
    private final UUID campaignId;
    private final UUID organizationId;
    private final BigDecimal amountSpent;
    private final Boolean onTime;

    public EvidenceApprovedEvent(UUID triggeredBy, UUID evidenceId, UUID campaignId,
            UUID organizationId, BigDecimal amountSpent, Boolean onTime) {
        super(triggeredBy);
        this.evidenceId = evidenceId;
        this.campaignId = campaignId;
        this.organizationId = organizationId;
        this.amountSpent = amountSpent;
        this.onTime = onTime;
    }
}
