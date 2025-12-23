package com.seffafbagis.api.dto.response.transparency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for transparency score response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransparencyScoreResponse {

    private UUID id;
    private UUID organizationId;
    private String organizationName;
    private BigDecimal currentScore;
    private String scoreLevel;
    private Integer totalCampaigns;
    private Integer completedCampaigns;
    private Integer onTimeReports;
    private Integer lateReports;
    private Integer approvedEvidences;
    private Integer rejectedEvidences;
    private java.time.LocalDateTime lastCalculatedAt;
}
