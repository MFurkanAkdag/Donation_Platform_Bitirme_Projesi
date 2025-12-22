package com.seffafbagis.api.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportListResponse {
    private List<ReportResponse> reports;
    private long totalCount;
    private int totalPages;
    private int currentPage;

    // Status summary counts
    private long pendingCount;
    private long investigatingCount;
    private long resolvedCount;
    private long dismissedCount;
}
