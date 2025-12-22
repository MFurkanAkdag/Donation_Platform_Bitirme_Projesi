package com.seffafbagis.api.dto.response.evidence;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class EvidenceDetailResponse extends EvidenceResponse {
    private String vendorTaxNumber;
    private List<EvidenceDocumentResponse> documents;
    private OffsetDateTime reviewedAt;
    private String reviewedByName;
    private String rejectionReason;
}
