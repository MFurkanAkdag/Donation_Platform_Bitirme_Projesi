package com.seffafbagis.api.dto.response.organization;

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
public class OrganizationSummaryResponse {
    private UUID id;
    private String legalName;
    private String logoUrl;
    private BigDecimal transparencyScore;
    private Boolean isVerified;
}
