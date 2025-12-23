package com.seffafbagis.api.dto.response.evidence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EvidenceDetailResponse extends EvidenceResponse {
    private String vendorTaxNumber;
    private List<EvidenceDocumentResponse> documents;
    private String reviewedByName;
}
