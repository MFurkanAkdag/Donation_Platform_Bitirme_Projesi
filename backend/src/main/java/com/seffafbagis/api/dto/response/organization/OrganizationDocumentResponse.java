package com.seffafbagis.api.dto.response.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDocumentResponse {
    private UUID id;
    private String documentType;
    private String documentName;
    private String fileUrl;
    private Integer fileSize;
    private String mimeType;
    private Boolean isVerified;
    private LocalDateTime verifiedAt;
    private LocalDate expiresAt;
    private LocalDateTime uploadedAt;
    private String verifiedBy;

    // Computed
    private Boolean isExpired;
    private Boolean isExpiringSoon; // Within 30 days
}
