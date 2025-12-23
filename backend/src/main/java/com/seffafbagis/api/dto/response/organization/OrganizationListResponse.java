package com.seffafbagis.api.dto.response.organization;

import com.seffafbagis.api.enums.OrganizationType;
import com.seffafbagis.api.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for organization list response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationListResponse {
    private UUID id;
    private OrganizationType organizationType;
    private String legalName;
    private String tradeName;
    private String logoUrl;
    private VerificationStatus verificationStatus;
    private Boolean isFeatured;
    private BigDecimal transparencyScore;
    private Long campaignCount;
    private LocalDateTime createdAt;

    // Compatibility fields
    private BigDecimal totalDonations;
}
