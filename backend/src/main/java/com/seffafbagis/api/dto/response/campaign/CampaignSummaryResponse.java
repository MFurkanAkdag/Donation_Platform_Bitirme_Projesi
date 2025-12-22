package com.seffafbagis.api.dto.response.campaign;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CampaignSummaryResponse {
    private UUID id;
    private String title;
    private String slug;
    private String coverImageUrl;
    private BigDecimal targetAmount;
    private BigDecimal collectedAmount;
}
