package com.seffafbagis.api.event;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event published when a transparency score changes.
 * This can be used by listeners to perform additional actions
 * like sending notifications, logging, or triggering other workflows.
 */
@Getter
public class TransparencyScoreChangedEvent extends BaseEvent {

    /**
     * Organization ID whose score changed
     */
    private final UUID organizationId;

    /**
     * Score before the change (can be null for initial scores)
     */
    private final BigDecimal previousScore;

    /**
     * Score after the change
     */
    private final BigDecimal newScore;

    /**
     * Reason for the change (e.g., "EVIDENCE_APPROVED", "CAMPAIGN_COMPLETED")
     */
    private final String changeReason;

    public TransparencyScoreChangedEvent(UUID triggeredBy, UUID organizationId,
            BigDecimal previousScore, BigDecimal newScore, String changeReason) {
        super(triggeredBy);
        this.organizationId = organizationId;
        this.previousScore = previousScore;
        this.newScore = newScore;
        this.changeReason = changeReason;
    }

    /**
     * Calculate the change amount
     */
    public BigDecimal getChangeAmount() {
        if (previousScore == null) {
            return newScore;
        }
        return newScore.subtract(previousScore);
    }

    /**
     * Check if the score increased
     */
    public boolean isPositiveChange() {
        return getChangeAmount().compareTo(BigDecimal.ZERO) > 0;
    }
}
