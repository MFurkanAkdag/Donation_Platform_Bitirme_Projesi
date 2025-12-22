package com.seffafbagis.api.dto.response.application;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApplicationDetailResponse extends ApplicationResponse {

    private List<ApplicationDocumentResponse> documents;

    private UUID assignedOrganizationId;
    private String assignedOrganizationName;

    private UUID assignedCampaignId;
    private String assignedCampaignTitle;

    private OffsetDateTime reviewedAt;
    private String reviewedByName;

    private String rejectionReason; // If we store it somewhere, or maybe it's review notes?
    // The prompt says "rejectionReason (if rejected)".
    // Wait, where is rejectionReason stored in Entity?
    // Ah, Entity doesn't have rejectionReason explicitly listed in the prompt's
    // Entity section.
    // However, Request DTO has it. It might be stored in 'description' or a
    // separate field or maybe I missed it.
    // Let's re-read Entity prompt.
    // "Fields: title, description, requestedAmount, status, locationCity,
    // locationDistrict, householdSize, urgencyLevel (1-5), reviewedAt"
    // Does not list rejectionReason.
    // But ReviewApplicationRequest has it.
    // Maybe it's not stored or stored in a separate review/audit table? Or maybe I
    // should add it to Entity.
    // Given the ReviewRequest has it, and DetailResponse has it, it must be stored.
    // I should add `rejectionReason` to Application entity.
    // I'll assume I need to add it to generic Application entity as a field.
}
