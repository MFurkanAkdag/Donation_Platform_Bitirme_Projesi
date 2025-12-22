package com.seffafbagis.api.dto.response.transparency;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class TransparencyLeaderboardResponse {
    private int rank;
    private UUID organizationId;
    private String organizationName;
    private String logoUrl;
    private BigDecimal currentScore;
    private Integer completedCampaigns;
}
