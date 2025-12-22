package com.seffafbagis.api.dto.response.campaign;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CampaignProgressResponse {
    private BigDecimal targetAmount;
    private BigDecimal collectedAmount;
    private BigDecimal progressPercentage;
    private Integer donorCount;
    private Long daysRemaining;
    private Boolean isCompleted;
}
