package com.seffafbagis.api.dto.request.donation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchBankTransferRequest {

    @NotNull(message = "Reference code is required")
    private String referenceCode;

    @NotNull(message = "Actual amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal actualAmount;

    private String senderName;
    private String senderIban;
    private String notes;
}
