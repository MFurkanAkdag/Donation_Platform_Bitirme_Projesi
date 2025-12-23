package com.seffafbagis.api.dto.request.report;

import com.seffafbagis.api.enums.ReportEntityType;
import com.seffafbagis.api.enums.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

public class CreateReportRequest {

    @NotNull(message = "Report type cannot be null")
    private ReportType reportType;

    @NotNull(message = "Entity type cannot be null")
    private ReportEntityType entityType;

    @NotNull(message = "Entity ID cannot be null")
    private UUID entityId;

    @NotBlank(message = "Reason cannot be blank")
    @Size(max = 255, message = "Reason cannot specify 255 characters")
    private String reason;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private List<String> evidenceUrls;

    private boolean anonymous = false;

    public CreateReportRequest() {
    }

    public CreateReportRequest(ReportType reportType, ReportEntityType entityType, UUID entityId, String reason,
            String description, List<String> evidenceUrls, boolean anonymous) {
        this.reportType = reportType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.reason = reason;
        this.description = description;
        this.evidenceUrls = evidenceUrls;
        this.anonymous = anonymous;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public ReportEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(ReportEntityType entityType) {
        this.entityType = entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getEvidenceUrls() {
        return evidenceUrls;
    }

    public void setEvidenceUrls(List<String> evidenceUrls) {
        this.evidenceUrls = evidenceUrls;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }
}
