package com.seffafbagis.api.dto.request.evidence;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateEvidenceDocumentRequest {
    @NotBlank
    private String fileName;
    @NotBlank
    private String fileUrl;
    private Long fileSize;
    private String mimeType;
    private Boolean isPrimary = false;
}
