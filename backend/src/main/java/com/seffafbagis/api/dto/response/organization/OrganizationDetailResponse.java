package com.seffafbagis.api.dto.response.organization;

import com.seffafbagis.api.enums.OrganizationType;
import com.seffafbagis.api.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for organization detail response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDetailResponse {
    private UUID id;
    private UUID userId;
    private OrganizationType organizationType;
    private String legalName;
    private String tradeName;
    private String taxNumber;
    private String derbisNumber;
    private String mersisNumber;
    private LocalDate establishmentDate;
    private String description;
    private String missionStatement;
    private String logoUrl;
    private String bannerUrl;
    private String websiteUrl;
    private VerificationStatus verificationStatus;
    private LocalDateTime verifiedAt;
    private String rejectionReason;
    private Integer resubmissionCount;
    private OffsetDateTime lastResubmissionAt;
    private Boolean isFeatured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal transparencyScore;

    // Related entities
    private List<OrganizationContactResponse> contacts;
    private List<OrganizationDocumentResponse> documents;
    private List<OrganizationBankAccountResponse> bankAccounts;

    // Statistics
    private Long totalCampaigns;
    private Long activeCampaigns;
    private BigDecimal totalDonationsReceived;

    // Legacy fields for backward compatibility
    private String mission;
    private String vision;
    private String foundationDate;
    private String address;
    private String city;
    private String district;
    private String postalCode;
    private String phoneNumber;
    private String email;
    private Integer campaignCount;
    private Integer activeCampaignCount;
    private BigDecimal totalDonations;
    private Integer totalDonors;
    private List<String> categories;
}
