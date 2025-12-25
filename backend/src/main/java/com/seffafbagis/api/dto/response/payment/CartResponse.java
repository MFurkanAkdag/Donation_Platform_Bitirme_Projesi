package com.seffafbagis.api.dto.response.payment;

import com.seffafbagis.api.dto.response.donation.DonationResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private UUID sessionId;
    private BigDecimal totalAmount;
    private String currency;
    private List<DonationResponse> donations;
    private Integer itemCount;
    private OffsetDateTime createdAt;
}
