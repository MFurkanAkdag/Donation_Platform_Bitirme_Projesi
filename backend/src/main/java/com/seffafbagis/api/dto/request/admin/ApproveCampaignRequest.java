package com.seffafbagis.api.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ApproveCampaignRequest {

    @NotBlank(message = "Decision is required")
    @Pattern(regexp = "^(APPROVE|REJECT)$", message = "Decision must be APPROVE or REJECT")
    private String decision;

    @Size(max = 1000, message = "Reason must be less than 1000 characters")
    private String reason;

    @Size(max = 2000, message = "Notes must be less than 2000 characters")
    private String notes;

    private Boolean notifyOrganization = true;

    public ApproveCampaignRequest() {
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getNotifyOrganization() {
        return notifyOrganization;
    }

    public void setNotifyOrganization(Boolean notifyOrganization) {
        this.notifyOrganization = notifyOrganization;
    }
}
