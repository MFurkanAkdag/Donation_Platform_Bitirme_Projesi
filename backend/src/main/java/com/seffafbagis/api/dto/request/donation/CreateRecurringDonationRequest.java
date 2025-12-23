package com.seffafbagis.api.dto.request.donation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating a recurring donation subscription.
 */
@Data
public class CreateRecurringDonationRequest {

    private UUID campaignId;

    private UUID organizationId;

    @NotNull(message = "Bağış tutarı gereklidir")
    @DecimalMin(value = "1.00", message = "Minimum bağış tutarı 1 TL")
    private BigDecimal amount;

    @NotBlank(message = "Bağış sıklığı gereklidir")
    private String frequency; // 'weekly', 'monthly', 'yearly'

    private UUID donationTypeId;

    private String cardToken;
}
