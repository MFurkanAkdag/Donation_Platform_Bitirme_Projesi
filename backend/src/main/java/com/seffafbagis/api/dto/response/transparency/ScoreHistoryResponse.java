package com.seffafbagis.api.dto.response.transparency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for transparency score history response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreHistoryResponse {

    private UUID id;
    private BigDecimal previousScore;
    private BigDecimal newScore;
    private BigDecimal changeAmount;
    private String changeReason;
    private String relatedEntityType;
    private UUID relatedEntityId;
    private String campaignTitle;
    private OffsetDateTime createdAt;
}
