package com.seffafbagis.api.event;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event published when evidence is uploaded for a campaign.
 */
@Getter
public class EvidenceUploadedEvent extends BaseEvent {

    private final UUID evidenceId;
    private final UUID campaignId;
    private final UUID organizationId;
    private final BigDecimal amountDocumented;

    public EvidenceUploadedEvent(UUID triggeredBy, UUID evidenceId, UUID campaignId,
            UUID organizationId, BigDecimal amountDocumented) {
        super(triggeredBy);
        this.evidenceId = evidenceId;
        this.campaignId = campaignId;
        this.organizationId = organizationId;
        this.amountDocumented = amountDocumented;
    }
}
