package com.seffafbagis.api.dto.response.transparency;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for transparency score response.
 */
public class TransparencyScoreResponse {

    private UUID id;
    private UUID organizationId;
    private BigDecimal currentScore;
    private BigDecimal evidenceScore;
    private BigDecimal timelinessScore;
    private BigDecimal reportScore;
    private Integer totalCampaigns;
    private Integer completedCampaigns;
    private Integer totalEvidences;
    private Integer approvedEvidences;
    private String grade;

    // Getters and Setters

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

    public BigDecimal getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(BigDecimal currentScore) {
        this.currentScore = currentScore;
    }

    public BigDecimal getEvidenceScore() {
        return evidenceScore;
    }

    public void setEvidenceScore(BigDecimal evidenceScore) {
        this.evidenceScore = evidenceScore;
    }

    public BigDecimal getTimelinessScore() {
        return timelinessScore;
    }

    public void setTimelinessScore(BigDecimal timelinessScore) {
        this.timelinessScore = timelinessScore;
    }

    public BigDecimal getReportScore() {
        return reportScore;
    }

    public void setReportScore(BigDecimal reportScore) {
        this.reportScore = reportScore;
    }

    public Integer getTotalCampaigns() {
        return totalCampaigns;
    }

    public void setTotalCampaigns(Integer totalCampaigns) {
        this.totalCampaigns = totalCampaigns;
    }

    public Integer getCompletedCampaigns() {
        return completedCampaigns;
    }

    public void setCompletedCampaigns(Integer completedCampaigns) {
        this.completedCampaigns = completedCampaigns;
    }

    public Integer getTotalEvidences() {
        return totalEvidences;
    }

    public void setTotalEvidences(Integer totalEvidences) {
        this.totalEvidences = totalEvidences;
    }

    public Integer getApprovedEvidences() {
        return approvedEvidences;
    }

    public void setApprovedEvidences(Integer approvedEvidences) {
        this.approvedEvidences = approvedEvidences;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
