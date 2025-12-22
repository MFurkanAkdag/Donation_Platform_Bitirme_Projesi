package com.seffafbagis.api.event;

import lombok.Getter;

import java.util.UUID;

/**
 * Event published when evidence is rejected by admin.
 */
@Getter
public class EvidenceRejectedEvent extends BaseEvent {

    private final UUID evidenceId;
    private final UUID campaignId;
    private final UUID organizationId;
    private final String rejectionReason;

    public EvidenceRejectedEvent(UUID triggeredBy, UUID evidenceId, UUID campaignId,
            UUID organizationId, String rejectionReason) {
        super(triggeredBy);
        this.evidenceId = evidenceId;
        this.campaignId = campaignId;
        this.organizationId = organizationId;
        this.rejectionReason = rejectionReason;
    }
}
