package com.seffafbagis.api.dto.response.favorite;

import com.seffafbagis.api.entity.favorite.FavoriteOrganization;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// We assume Organization details will come from an external simple object or map since we don't have the Organization entity in this context directly,
// but usually we would map it. For now, we follow the requirement to have specific fields.

public class FavoriteOrganizationResponse {
    private UUID organizationId;
    private String organizationName;
    private String organizationLogo;
    private String organizationDescription;
    private String verificationStatus;
    private Integer activeCampaignsCount;
    private BigDecimal totalRaised;
    private LocalDateTime favoritedAt;

    public static FavoriteOrganizationResponse fromEntity(FavoriteOrganization favorite, UUID organizationId,
            String name, String logo, String description, String status, Integer campaigns, BigDecimal raised) {
        FavoriteOrganizationResponse response = new FavoriteOrganizationResponse();
        response.setOrganizationId(organizationId);
        response.setOrganizationName(name);
        response.setOrganizationLogo(logo);
        response.setOrganizationDescription(description);
        response.setVerificationStatus(status);
        response.setActiveCampaignsCount(campaigns);
        response.setTotalRaised(raised);
        response.setFavoritedAt(favorite.getCreatedAt());
        return response;
    }

    // Getters and Setters
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

    public String getOrganizationLogo() {
        return organizationLogo;
    }

    public void setOrganizationLogo(String organizationLogo) {
        this.organizationLogo = organizationLogo;
    }

    public String getOrganizationDescription() {
        return organizationDescription;
    }

    public void setOrganizationDescription(String organizationDescription) {
        this.organizationDescription = organizationDescription;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Integer getActiveCampaignsCount() {
        return activeCampaignsCount;
    }

    public void setActiveCampaignsCount(Integer activeCampaignsCount) {
        this.activeCampaignsCount = activeCampaignsCount;
    }

    public BigDecimal getTotalRaised() {
        return totalRaised;
    }

    public void setTotalRaised(BigDecimal totalRaised) {
        this.totalRaised = totalRaised;
    }

    public LocalDateTime getFavoritedAt() {
        return favoritedAt;
    }

    public void setFavoritedAt(LocalDateTime favoritedAt) {
        this.favoritedAt = favoritedAt;
    }
}
