package com.seffafbagis.api.dto.response.campaign;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for campaign detail response.
 */

@Getter
@Setter
public class CampaignDetailResponse {

    private UUID id;
    private String title;
    private String slug;
    private String description;
    private String shortDescription;
    private String coverImageUrl;
    private BigDecimal targetAmount;
    private BigDecimal collectedAmount;
    private Integer donorCount;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime completedAt;
    private Boolean isFeatured;
    private Boolean isUrgent;
    private String organizationName;
    private UUID organizationId;
    private BigDecimal organizationTransparencyScore;
    private List<String> categories;
    private List<String> donationTypes;
    private LocalDateTime createdAt;
    private Integer evidenceDeadlineDays;
    private LocalDateTime approvedAt;

}
