package com.seffafbagis.api.dto.response.report;

import com.seffafbagis.api.enums.ReportEntityType;
import com.seffafbagis.api.enums.ReportPriority;
import com.seffafbagis.api.enums.ReportStatus;
import com.seffafbagis.api.enums.ReportType;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ReportResponse {
    private UUID id;
    private ReportType reportType;
    private String reportTypeName;
    private ReportEntityType entityType;
    private String entityTypeName;
    private UUID entityId;
    private String entityName;
    private String reason;
    private ReportPriority priority;
    private String priorityName;
    private ReportStatus status;
    private String statusName;
    private OffsetDateTime createdAt;
    private boolean isAnonymous;

    public ReportResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public String getReportTypeName() {
        return reportTypeName;
    }

    public void setReportTypeName(String reportTypeName) {
        this.reportTypeName = reportTypeName;
    }

    public ReportEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(ReportEntityType entityType) {
        this.entityType = entityType;
    }

    public String getEntityTypeName() {
        return entityTypeName;
    }

    public void setEntityTypeName(String entityTypeName) {
        this.entityTypeName = entityTypeName;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ReportPriority getPriority() {
        return priority;
    }

    public void setPriority(ReportPriority priority) {
        this.priority = priority;
    }

    public String getPriorityName() {
        return priorityName;
    }

    public void setPriorityName(String priorityName) {
        this.priorityName = priorityName;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }
}
