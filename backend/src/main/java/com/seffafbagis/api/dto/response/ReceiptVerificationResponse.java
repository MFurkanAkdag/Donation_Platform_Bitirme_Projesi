package com.seffafbagis.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptVerificationResponse {
    private boolean valid;
    private String receiptCode;
    private BigDecimal donationAmount;
    private String donorName;
    private LocalDateTime date;
    private String campaignTitle;
}
