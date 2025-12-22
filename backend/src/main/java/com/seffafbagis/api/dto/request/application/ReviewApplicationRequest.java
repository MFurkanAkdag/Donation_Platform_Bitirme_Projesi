package com.seffafbagis.api.dto.request.application;

import com.seffafbagis.api.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewApplicationRequest {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    private String rejectionReason;

    private UUID assignedOrganizationId;

    private String notes;
}
