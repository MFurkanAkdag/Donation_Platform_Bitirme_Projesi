package com.seffafbagis.api.event;

import lombok.Getter;

import java.util.UUID;

/**
 * Event published when a beneficiary application is submitted.
 */
@Getter
public class ApplicationSubmittedEvent extends BaseEvent {

    private final UUID applicationId;
    private final UUID applicantId;
    private final String categoryName;

    public ApplicationSubmittedEvent(UUID triggeredBy, UUID applicationId, UUID applicantId,
            String categoryName) {
        super(triggeredBy);
        this.applicationId = applicationId;
        this.applicantId = applicantId;
        this.categoryName = categoryName;
    }
}
