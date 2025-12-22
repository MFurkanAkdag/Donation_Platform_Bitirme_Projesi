package com.seffafbagis.api.dto.request.campaign;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CampaignSearchRequest {
    private String keyword;
    private String categorySlug;
    private String donationTypeCode;
    private String city;
    private Boolean isUrgent;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String sortBy; // newest, ending_soon, most_funded, most_donors

    public String getKeyword() {
        return keyword;
    }
}
