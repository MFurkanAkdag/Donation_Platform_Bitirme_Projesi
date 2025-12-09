package com.seffafbagis.api.dto.response.campaign;

import java.math.BigDecimal;

public class CampaignStatistics {
    private Long totalCampaigns;
    private Long pendingApprovals;
    private Long activeCampaigns;
    private Long completedCampaigns;
    private Long rejectedCampaigns;
    private BigDecimal totalRaisedAmount;

    public CampaignStatistics() {
    }

    public Long getTotalCampaigns() {
        return totalCampaigns;
    }

    public void setTotalCampaigns(Long totalCampaigns) {
        this.totalCampaigns = totalCampaigns;
    }

    public Long getPendingApprovals() {
        return pendingApprovals;
    }

    public void setPendingApprovals(Long pendingApprovals) {
        this.pendingApprovals = pendingApprovals;
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

    public Long getRejectedCampaigns() {
        return rejectedCampaigns;
    }

    public void setRejectedCampaigns(Long rejectedCampaigns) {
        this.rejectedCampaigns = rejectedCampaigns;
    }

    public BigDecimal getTotalRaisedAmount() {
        return totalRaisedAmount;
    }

    public void setTotalRaisedAmount(BigDecimal totalRaisedAmount) {
        this.totalRaisedAmount = totalRaisedAmount;
    }
}
