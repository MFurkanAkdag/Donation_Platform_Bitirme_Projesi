package com.seffafbagis.api.controller.application;

import com.seffafbagis.api.dto.request.application.*;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.application.*;
import com.seffafbagis.api.enums.ApplicationStatus;
import com.seffafbagis.api.service.application.ApplicationDocumentService;
import com.seffafbagis.api.service.application.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationDocumentService applicationDocumentService;

    // Applicant Endpoints

    @PostMapping("/api/v1/applications")
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApplication(
            @Valid @RequestBody CreateApplicationRequest request) {
        ApplicationResponse response = applicationService.createApplication(request);
        return ResponseEntity.ok(ApiResponse.success("Application submitted successfully", response));
    }

    @GetMapping("/api/v1/applications/my")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getMyApplications() {
        List<ApplicationResponse> response = applicationService.getMyApplications();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/applications/my/{id}")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> getMyApplication(@PathVariable UUID id) {
        ApplicationDetailResponse response = applicationService.getMyApplication(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/api/v1/applications/my/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplication(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateApplicationRequest request) {
        ApplicationResponse response = applicationService.updateApplication(id, request);
        return ResponseEntity.ok(ApiResponse.success("Application updated successfully", response));
    }

    @DeleteMapping("/api/v1/applications/my/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelApplication(@PathVariable UUID id) {
        applicationService.cancelApplication(id);
        return ResponseEntity.ok(ApiResponse.success("Application cancelled successfully"));
    }

    @PostMapping("/api/v1/applications/my/{id}/documents")
    public ResponseEntity<ApiResponse<Void>> addDocument(
            @PathVariable UUID id,
            @Valid @RequestBody DocumentRequest request) {
        applicationService.addDocument(id, request);
        return ResponseEntity.ok(ApiResponse.success("Document added successfully"));
    }

    @DeleteMapping("/api/v1/applications/my/{id}/documents/{docId}")
    public ResponseEntity<ApiResponse<Void>> removeDocument(
            @PathVariable UUID id,
            @PathVariable UUID docId) {
        applicationService.removeDocument(id, docId);
        return ResponseEntity.ok(ApiResponse.success("Document removed successfully"));
    }

    // Organization Endpoints (FOUNDATION role)

    @PreAuthorize("hasRole('FOUNDATION')")
    @GetMapping("/api/v1/applications/assigned")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getAssignedApplications(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ApplicationResponse> response = applicationService.getAssignedApplications(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('FOUNDATION')")
    @PostMapping("/api/v1/applications/{id}/assign-campaign")
    public ResponseEntity<ApiResponse<Void>> assignToCampaign(
            @PathVariable UUID id,
            @Valid @RequestBody AssignToCampaignRequest request) {
        applicationService.assignToCampaign(id, request);
        return ResponseEntity.ok(ApiResponse.success("Application assigned to campaign successfully"));
    }

    @PreAuthorize("hasRole('FOUNDATION')")
    @PostMapping("/api/v1/applications/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> completeApplication(
            @PathVariable UUID id,
            @Valid @RequestBody CompleteApplicationRequest request) {
        applicationService.completeApplication(id, request);
        return ResponseEntity.ok(ApiResponse.success("Application marked as completed"));
    }

    // Admin Endpoints

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/v1/admin/applications")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getAllApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ApplicationResponse> response = applicationService.getAllApplications(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/v1/admin/applications/{id}")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> getApplicationDetail(@PathVariable UUID id) {
        ApplicationDetailResponse response = applicationService.getApplicationDetail(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/v1/admin/applications/{id}/review")
    public ResponseEntity<ApiResponse<Void>> reviewApplication(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewApplicationRequest request) {
        applicationService.reviewApplication(id, request);
        return ResponseEntity.ok(ApiResponse.success("Application review submitted"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/v1/admin/applications/{id}/assign-organization")
    public ResponseEntity<ApiResponse<Void>> assignToOrganization(
            @PathVariable UUID id,
            @RequestParam UUID organizationId) {
        // Using request param or body? Prompt says POST.
        // Params simplify DTO creation if just ID.
        // Prompt DTOs list didn't mention AssignOrganizationRequest, only
        // AssignToCampaignRequest.
        // But prompt says "POST /api/v1/admin/applications/{id}/assign-organization -
        // Assign to org"
        // and in Service "assignToOrganization(UUID applicationId, UUID
        // organizationId)".
        // So I'll use RequestParam or a simple body wrapper. I'll use RequestParam for
        // simplicity consistent with REST unless complex body.
        applicationService.assignToOrganization(id, organizationId);
        return ResponseEntity.ok(ApiResponse.success("Application assigned to organization"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/v1/admin/applications/stats")
    public ResponseEntity<ApiResponse<ApplicationStatsResponse>> getApplicationStats() {
        ApplicationStatsResponse response = applicationService.getApplicationStats();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/v1/admin/applications/{id}/documents/{docId}/verify")
    public ResponseEntity<ApiResponse<Void>> verifyDocument(
            @PathVariable UUID id,
            @PathVariable UUID docId) {
        applicationDocumentService.verifyDocument(docId); // Service method was implemented in doc service
        return ResponseEntity.ok(ApiResponse.success("Document verified"));
    }

}
