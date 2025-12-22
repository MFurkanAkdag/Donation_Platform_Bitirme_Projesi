package com.seffafbagis.api.dto.request.organization;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ResubmitVerificationRequest {

    private String additionalNotes;

    // List of newly uploaded document IDs to include in resubmission
    private List<UUID> newDocumentIds;
}
