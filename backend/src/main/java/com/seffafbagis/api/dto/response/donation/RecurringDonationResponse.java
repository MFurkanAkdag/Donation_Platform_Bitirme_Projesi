package com.seffafbagis.api.dto.response.donation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for recurring donation response.
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringDonationResponse {

    private UUID id;
    private UUID campaignId;
    private String campaignTitle;
    private UUID organizationId;
    private String organizationName;
    private String donationTypeCode;
    private BigDecimal amount;
    private String currency;
    private String frequency;
    private LocalDate nextPaymentDate;
    private LocalDate lastPaymentDate;
    private BigDecimal totalDonated;
    private Integer paymentCount;
    private String status;
    private OffsetDateTime createdAt;

}