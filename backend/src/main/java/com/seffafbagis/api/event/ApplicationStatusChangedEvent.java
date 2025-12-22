package com.seffafbagis.api.event;

import com.seffafbagis.api.enums.ApplicationStatus;
import lombok.Getter;

import java.util.UUID;

/**
 * Event published when a beneficiary application status changes.
 */
@Getter
public class ApplicationStatusChangedEvent extends BaseEvent {

    private final UUID applicationId;
    private final UUID applicantId;
    private final ApplicationStatus previousStatus;
    private final ApplicationStatus newStatus;

    public ApplicationStatusChangedEvent(UUID triggeredBy, UUID applicationId, UUID applicantId,
            ApplicationStatus previousStatus, ApplicationStatus newStatus) {
        super(triggeredBy);
        this.applicationId = applicationId;
        this.applicantId = applicantId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }
}
