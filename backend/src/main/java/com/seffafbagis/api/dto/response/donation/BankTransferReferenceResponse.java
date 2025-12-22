package com.seffafbagis.api.dto.response.donation;

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
public class BankTransferReferenceResponse {

    private UUID id;
    private String referenceCode;
    private String campaignTitle;
    private BigDecimal expectedAmount;
    private String status;
    private String senderName;
    private String senderIban;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private UUID matchedDonationId;
}
