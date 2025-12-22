package com.seffafbagis.api.dto.request.donation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecurringDonationRequest {

    @DecimalMin(value = "1.0", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Pattern(regexp = "^(weekly|monthly|yearly)$", message = "Frequency must be 'weekly', 'monthly', or 'yearly'")
    private String frequency;

    private UUID donationTypeId;
}
