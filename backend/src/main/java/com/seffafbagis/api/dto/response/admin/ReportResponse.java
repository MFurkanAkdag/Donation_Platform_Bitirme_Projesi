package com.seffafbagis.api.dto.response.admin;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReportResponse {

    private UUID id;
    private UUID reporterId;
    private String reporterEmail;
    private String reportType;
    private String targetType; // ORGANIZATION, CAMPAIGN, USER
    private UUID targetId;
    private String targetName;
    private String reason;
    private String description;
    private String status; // PENDING, ASSIGNED, RESOLVED, DISMISSED
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL
    private AdminOrganizationResponse.AdminUserSummary assignedTo;
    private LocalDateTime assignedAt;
    private String resolution;
    private String resolutionNotes;
    private AdminOrganizationResponse.AdminUserSummary resolvedBy;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;

    public ReportResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getReporterId() {
        return reporterId;
    }

    public void setReporterId(UUID reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public AdminOrganizationResponse.AdminUserSummary getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(AdminOrganizationResponse.AdminUserSummary assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
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

    public AdminOrganizationResponse.AdminUserSummary getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(AdminOrganizationResponse.AdminUserSummary resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
