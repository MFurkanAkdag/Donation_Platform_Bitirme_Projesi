package com.seffafbagis.api.dto.response.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

}
