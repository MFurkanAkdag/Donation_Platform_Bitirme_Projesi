package com.seffafbagis.api.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Cart item response (before checkout).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private UUID campaignId;
    private String campaignTitle;
    private BigDecimal amount;
    private String currency;
}
