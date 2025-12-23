package com.seffafbagis.api.dto.response.donation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for bank transfer info response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankTransferInfoResponse {

    private UUID id;
    private String referenceCode;
    private UUID campaignId;
    private String campaignTitle;
    private BigDecimal expectedAmount;
    private String bankName;
    private String accountHolder;
    private String iban;
    private String branchName;
    private String accountNumber;
    private OffsetDateTime expiresAt;
    private String instructions;

}
