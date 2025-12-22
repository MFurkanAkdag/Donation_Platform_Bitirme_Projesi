package com.seffafbagis.api.dto.request.report;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AssignReportRequest {

    @NotNull(message = "Assigned to admin ID cannot be null")
    private UUID assignedTo;

    private String notes;

    public AssignReportRequest() {
    }

    public AssignReportRequest(UUID assignedTo, String notes) {
        this.assignedTo = assignedTo;
        this.notes = notes;
    }

    public UUID getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
