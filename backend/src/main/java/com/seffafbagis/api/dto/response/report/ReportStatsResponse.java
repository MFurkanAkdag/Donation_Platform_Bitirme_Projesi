package com.seffafbagis.api.dto.response.report;

import lombok.Data;

import java.util.Map;

@Data
public class ReportStatsResponse {
    private Map<String, Long> byType;
    private Map<String, Long> byPriority;
    private Map<String, Long> byStatus;
    private Double averageResolutionTimeHours;
    private Long todayNewCount;
    private Long todayResolvedCount;
}
