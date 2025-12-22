package com.seffafbagis.api.dto.response.payment;

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
public class RefundResultResponse {
    private boolean success;
    private UUID transactionId;
    private BigDecimal refundedAmount;
    private String message;
}
