package com.seffafbagis.api.dto.response.evidence;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class EvidenceDocumentResponse {
    private UUID id;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String mimeType;
    private Boolean isPrimary;
    private OffsetDateTime uploadedAt;
}
