package com.seffafbagis.api.dto.request.report;

import com.seffafbagis.api.enums.ActionType;
import com.seffafbagis.api.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class ResolveReportRequest {

    @NotNull(message = "Resolution status cannot be null")
    private ReportStatus resolution; // RESOLVED or DISMISSED

    @NotNull(message = "Resolution notes cannot be null")
    @Size(max = 1000, message = "Resolution notes cannot exceed 1000 characters")
    private String resolutionNotes;

    private boolean takeAction = false;

    private ActionType actionType; // Required if takeAction is true

    public ResolveReportRequest() {
    }

    public ResolveReportRequest(ReportStatus resolution, String resolutionNotes, boolean takeAction,
            ActionType actionType) {
        this.resolution = resolution;
        this.resolutionNotes = resolutionNotes;
        this.takeAction = takeAction;
        this.actionType = actionType;
    }

    public ReportStatus getResolution() {
        return resolution;
    }

    public void setResolution(ReportStatus resolution) {
        this.resolution = resolution;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public boolean isTakeAction() {
        return takeAction;
    }

    public void setTakeAction(boolean takeAction) {
        this.takeAction = takeAction;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
}
