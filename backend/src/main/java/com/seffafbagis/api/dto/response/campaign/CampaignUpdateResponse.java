package com.seffafbagis.api.dto.response.campaign;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for campaign update response.
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class CampaignUpdateResponse {

    private UUID id;
    private String title;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private String createdByName;
}
