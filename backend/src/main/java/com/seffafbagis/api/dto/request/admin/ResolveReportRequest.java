package com.seffafbagis.api.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ResolveReportRequest {

    @NotBlank(message = "Resolution is required")
    @Pattern(regexp = "^(RESOLVED|DISMISSED|ACTION_TAKEN)$", message = "Invalid resolution type")
    private String resolution;

    @NotBlank(message = "Resolution notes are required")
    @Size(max = 2000, message = "Resolution notes must be less than 2000 characters")
    private String resolutionNotes;

    @Size(max = 1000, message = "Action taken description must be less than 1000 characters")
    private String actionTaken;

    private Boolean notifyReporter = true;

    public ResolveReportRequest() {
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public Boolean getNotifyReporter() {
        return notifyReporter;
    }

    public void setNotifyReporter(Boolean notifyReporter) {
        this.notifyReporter = notifyReporter;
    }
}
