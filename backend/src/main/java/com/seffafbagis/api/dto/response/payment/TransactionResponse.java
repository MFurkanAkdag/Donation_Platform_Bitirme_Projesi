package com.seffafbagis.api.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private UUID donationId;
    private String paymentMethod;
    private String provider;
    private String providerTransactionId;
    private BigDecimal amount;
    private BigDecimal feeAmount;
    private BigDecimal netAmount;
    private String status;
    private String cardLastFour;
    private String cardBrand;
    private boolean is3dSecure;
    private LocalDateTime processedAt;
}
