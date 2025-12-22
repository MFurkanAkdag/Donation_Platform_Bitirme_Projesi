package com.seffafbagis.api.dto.response.application;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ApplicationDocumentResponse {

    private UUID id;
    private String documentType;
    private String documentTypeName; // Readable name
    private String fileName;
    private String fileUrl;
    private Boolean isVerified;
    private OffsetDateTime uploadedAt;
}
