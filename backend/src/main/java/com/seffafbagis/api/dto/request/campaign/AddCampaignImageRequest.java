package com.seffafbagis.api.dto.request.campaign;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for adding an image to a campaign.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCampaignImageRequest {

    @NotBlank(message = "Resim URL gereklidir")
    private String imageUrl;

    private String thumbnailUrl;

    private String altText;

    private String caption;

    private Boolean isCover = false;

    private Integer sortOrder = 0;

    private Integer displayOrder = 0;
}
