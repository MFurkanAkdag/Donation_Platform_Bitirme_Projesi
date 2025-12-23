package com.seffafbagis.api.dto.request.donation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for initiating a bank transfer donation.
 */
@Data
public class InitiateBankTransferRequest {

    @NotNull(message = "Kampanya ID gereklidir")
    private UUID campaignId;

    @NotNull(message = "Bağış tutarı gereklidir")
    @DecimalMin(value = "1.00", message = "Minimum bağış tutarı 1 TL")
    private BigDecimal amount;

    private UUID donationTypeId;

    @NotBlank(message = "Gönderici adı gereklidir")
    private String senderName;

    private String senderIban;

    private Boolean isAnonymous = false;
}
