package com.seffafbagis.api.dto.response.evidence;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CampaignEvidenceSummaryResponse {
    private long totalEvidences;
    private long approvedCount;
    private long pendingCount;
    private BigDecimal totalAmountDocumented;
    private LocalDateTime deadline;
    private long daysRemaining;
    private boolean isOverdue;
}
