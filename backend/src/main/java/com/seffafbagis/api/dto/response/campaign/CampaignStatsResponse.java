package com.seffafbagis.api.dto.response.campaign;

import java.math.BigDecimal;

/**
 * DTO for campaign statistics response.
 */
public class CampaignStatsResponse {

    private Long totalCampaigns;
    private Long activeCampaigns;
    private Long completedCampaigns;
    private BigDecimal totalCollected;
    private Long totalDonors;
    private Long totalDonations;
    private BigDecimal averageDonation;
    private Long pendingCampaigns;

    // Getters and Setters

    public Long getTotalCampaigns() {
        return totalCampaigns;
    }

    public void setTotalCampaigns(Long totalCampaigns) {
        this.totalCampaigns = totalCampaigns;
    }

    public Long getActiveCampaigns() {
        return activeCampaigns;
    }

    public void setActiveCampaigns(Long activeCampaigns) {
        this.activeCampaigns = activeCampaigns;
    }

    public Long getCompletedCampaigns() {
        return completedCampaigns;
    }

    public void setCompletedCampaigns(Long completedCampaigns) {
        this.completedCampaigns = completedCampaigns;
    }

    public BigDecimal getTotalCollected() {
        return totalCollected;
    }

    public void setTotalCollected(BigDecimal totalCollected) {
        this.totalCollected = totalCollected;
    }

    public Long getTotalDonors() {
        return totalDonors;
    }

    public void setTotalDonors(Long totalDonors) {
        this.totalDonors = totalDonors;
    }

    public Long getTotalDonations() {
        return totalDonations;
    }

    public void setTotalDonations(Long totalDonations) {
        this.totalDonations = totalDonations;
    }

    public BigDecimal getAverageDonation() {
        return averageDonation;
    }

    public void setAverageDonation(BigDecimal averageDonation) {
        this.averageDonation = averageDonation;
    }

    public Long getPendingCampaigns() {
        return pendingCampaigns;
    }

    public void setPendingCampaigns(Long pendingCampaigns) {
        this.pendingCampaigns = pendingCampaigns;
    }
}
