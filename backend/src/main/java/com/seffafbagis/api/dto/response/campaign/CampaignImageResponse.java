package com.seffafbagis.api.dto.response.campaign;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CampaignImageResponse {
    private UUID id;
    private String imageUrl;
    private String thumbnailUrl;
    private String caption;
    private Integer displayOrder;
}
