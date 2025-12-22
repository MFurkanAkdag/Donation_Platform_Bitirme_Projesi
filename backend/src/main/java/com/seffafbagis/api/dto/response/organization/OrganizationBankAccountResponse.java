package com.seffafbagis.api.dto.response.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationBankAccountResponse {
    private UUID id;
    private String bankName;
    private String bankCode;
    private String branchName;
    private String branchCode;
    private String branchCity;
    private String branchDistrict;
    private String accountHolder;
    private String accountNumber;
    private String iban;
    private String maskedIban; // TR** **** **** **** **** **34
    private String currency;
    private String accountType;
    private Boolean isPrimary;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}
