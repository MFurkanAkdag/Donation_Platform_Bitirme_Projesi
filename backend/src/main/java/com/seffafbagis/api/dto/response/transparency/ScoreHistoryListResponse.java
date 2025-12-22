package com.seffafbagis.api.dto.response.transparency;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
public class ScoreHistoryListResponse {
    private List<ScoreHistoryResponse> history;
    private int totalPages;
    private long totalElements;
    private int currentPage;

    public static ScoreHistoryListResponse from(Page<ScoreHistoryResponse> page) {
        return ScoreHistoryListResponse.builder()
                .history(page.getContent())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .currentPage(page.getNumber())
                .build();
    }
}
