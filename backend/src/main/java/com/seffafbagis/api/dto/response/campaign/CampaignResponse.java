package com.seffafbagis.api.dto.response.campaign;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
public class CampaignResponse {
    private UUID id;
    private String title;
    private String slug;
    private String summary;
    private String description;
    private String coverImageUrl;
    private BigDecimal targetAmount;
    private BigDecimal collectedAmount;
    private BigDecimal currentAmount; // Alias for collectedAmount (AdminCampaignService compatibility)
    private Integer donorCount;
    private String currency;
    private String status;
    private String rejectionReason;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime approvedAt;
    private Boolean isUrgent;
    private Boolean isFeatured;
    private String locationCity;
    private LocalDateTime createdAt;

    private UUID organizationId;
    private String organizationName;
    private String organizationLogo;

    private BigDecimal progressPercentage;

    // Alias getter for currentAmount
    public BigDecimal getCurrentAmount() {
        return currentAmount != null ? currentAmount : collectedAmount;
    }
}
