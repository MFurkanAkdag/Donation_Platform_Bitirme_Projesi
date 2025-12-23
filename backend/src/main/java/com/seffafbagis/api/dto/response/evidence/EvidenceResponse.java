package com.seffafbagis.api.dto.response.evidence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for evidence detail response.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceResponse {

    private UUID id;
    private UUID campaignId;
    private String campaignTitle;
    private String evidenceType;
    private String title;
    private String description;
    private BigDecimal amountSpent;
    private LocalDate spendDate;
    private String vendorName;
    private String status;
    private String rejectionReason;
    private List<String> documentUrls;
    private UUID reviewedBy;
    private OffsetDateTime reviewedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String uploadedByName;
}
