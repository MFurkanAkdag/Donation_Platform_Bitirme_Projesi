package com.seffafbagis.api.dto.response.donation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for donation response.
 */
@Getter
@Setter
public class DonationResponse {

    private UUID id;
    private UUID campaignId;
    private String campaignTitle;
    private String campaignSlug;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private Boolean isAnonymous;
    private String donorDisplayName;
    private String donorMessage;
    private OffsetDateTime createdAt;
    private String donationTypeCode;
    private String donationTypeName;

}
