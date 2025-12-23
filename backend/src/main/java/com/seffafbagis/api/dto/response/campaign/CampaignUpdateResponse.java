package com.seffafbagis.api.dto.response.campaign;

import java.time.OffsetDateTime;
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
    private OffsetDateTime createdAt;
    private String createdByName;
}
