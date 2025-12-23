package com.seffafbagis.api.dto.request.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AddDocumentRequest {

    @NotBlank(message = "Document type is required")
    @Pattern(regexp = "^(tax_certificate|authorization|derbis_record|mersis_record|board_decision|other)$", message = "Invalid document type")
    private String documentType;

    @NotBlank(message = "Document name is required")
    @Size(max = 255)
    private String documentName;

    @NotBlank(message = "File URL is required")
    @Size(max = 500)
    private String fileUrl;

    private Integer fileSize;

    @Size(max = 100)
    private String mimeType;

    private LocalDate expiresAt;

    // Manual Getters
    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public LocalDate getExpiresAt() {
        return expiresAt;
    }
}
