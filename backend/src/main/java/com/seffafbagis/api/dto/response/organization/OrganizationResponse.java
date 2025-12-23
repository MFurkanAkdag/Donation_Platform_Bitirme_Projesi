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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    private UUID id;
    private UUID userId;
    private OrganizationType organizationType;
    private String legalName;
    private String tradeName;
    private String taxNumber;
    private String logoUrl;
    private String websiteUrl;
    private VerificationStatus verificationStatus;
    private String rejectionReason; // Added for Admin compatibility
    private LocalDateTime verifiedAt; // Added for Admin compatibility
    private UUID verifiedBy; // Added for Admin compatibility
    private Boolean isFeatured;
    private LocalDateTime createdAt;

    // For display
    private String organizationTypeName;
    private String verificationStatusName;

    // Added for backward compatibility with Admin module
    private String description;
    private Integer activeCampaignsCount;
    private BigDecimal totalRaised;

    // Alias methods for Admin module compatibility
    public String getName() {
        return legalName;
    }

    public String getLogo() {
        return logoUrl;
    }
}
