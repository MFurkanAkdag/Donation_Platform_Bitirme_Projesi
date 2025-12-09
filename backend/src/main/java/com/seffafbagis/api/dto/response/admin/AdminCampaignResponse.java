package com.seffafbagis.api.dto.response.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class AdminCampaignResponse {

    private UUID id;
    private UUID organizationId;
    private String organizationName;
    private String title;
    private String description;
    private String status;
    private String rejectionReason;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private Integer donorCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime approvedAt;
    private AdminOrganizationResponse.AdminUserSummary approvedBy;
    private Integer transparencyScore;
    private Integer evidenceCount;
    private Integer reportCount;
    private LocalDateTime createdAt;

    public AdminCampaignResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public Integer getDonorCount() {
        return donorCount;
    }

    public void setDonorCount(Integer donorCount) {
        this.donorCount = donorCount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public AdminOrganizationResponse.AdminUserSummary getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(AdminOrganizationResponse.AdminUserSummary approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Integer getTransparencyScore() {
        return transparencyScore;
    }

    public void setTransparencyScore(Integer transparencyScore) {
        this.transparencyScore = transparencyScore;
    }

    public Integer getEvidenceCount() {
        return evidenceCount;
    }

    public void setEvidenceCount(Integer evidenceCount) {
        this.evidenceCount = evidenceCount;
    }

    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
