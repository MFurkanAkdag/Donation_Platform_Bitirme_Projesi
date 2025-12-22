package com.seffafbagis.api.dto.request.report;

import com.seffafbagis.api.enums.ReportPriority;
import jakarta.validation.constraints.NotNull;

public class UpdateReportPriorityRequest {

    @NotNull(message = "Priority cannot be null")
    private ReportPriority priority;

    private String reason;

    public UpdateReportPriorityRequest() {
    }

    public UpdateReportPriorityRequest(ReportPriority priority, String reason) {
        this.priority = priority;
        this.reason = reason;
    }

    public ReportPriority getPriority() {
        return priority;
    }

    public void setPriority(ReportPriority priority) {
        this.priority = priority;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
