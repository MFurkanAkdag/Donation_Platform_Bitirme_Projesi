package com.seffafbagis.api.dto.response.campaign;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for campaign detail response.
 */

@Getter
@Setter
public class CampaignDetailResponse extends CampaignResponse {

    private String shortDescription;
    private LocalDateTime completedAt;
    private Integer beneficiaryCount;
    private Integer evidenceDeadlineDays;

    // Use summary response for nested organization detail
    private com.seffafbagis.api.dto.response.organization.OrganizationSummaryResponse organization;

    private List<com.seffafbagis.api.dto.response.category.CategoryResponse> categories;
    private List<com.seffafbagis.api.dto.response.category.DonationTypeResponse> donationTypes;

}
