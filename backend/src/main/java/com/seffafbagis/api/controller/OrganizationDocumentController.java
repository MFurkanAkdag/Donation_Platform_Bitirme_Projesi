package com.seffafbagis.api.controller;

import com.seffafbagis.api.dto.request.organization.AddDocumentRequest;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationDocumentResponse;
import com.seffafbagis.api.service.organization.OrganizationDocumentService;
import com.seffafbagis.api.service.organization.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organization/documents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FOUNDATION')")
public class OrganizationDocumentController {

    private final OrganizationDocumentService documentService;
    private final OrganizationService organizationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationDocumentResponse>>> getDocuments() {
        UUID orgId = organizationService.getMyOrganization().getId();
        List<OrganizationDocumentResponse> documents = documentService.getDocuments(orgId);
        return ResponseEntity.ok(ApiResponse.success(documents));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationDocumentResponse>> addDocument(
            @Valid @RequestBody AddDocumentRequest request) {
        OrganizationDocumentResponse response = documentService.addDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
    }
}
