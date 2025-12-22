package com.seffafbagis.api.dto.request.application;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignToCampaignRequest {

    @NotNull(message = "Campaign ID is required")
    private UUID campaignId;

    private String notes;
}
