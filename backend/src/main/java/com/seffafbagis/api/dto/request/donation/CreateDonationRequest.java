package com.seffafbagis.api.dto.request.donation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating a new donation.
 */
@Data
public class CreateDonationRequest {

    @NotNull(message = "Kampanya ID gereklidir")
    private UUID campaignId;

    @NotNull(message = "Bağış tutarı gereklidir")
    @DecimalMin(value = "1.00", message = "Minimum bağış tutarı 1 TL")
    private BigDecimal amount;

    private String currency;

    private UUID donationTypeId;

    private Boolean isAnonymous = false;

    private String donorMessage;

    private String donorDisplayName;
}
