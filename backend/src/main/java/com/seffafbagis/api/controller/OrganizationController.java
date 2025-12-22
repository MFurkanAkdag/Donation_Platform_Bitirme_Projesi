package com.seffafbagis.api.controller;

import com.seffafbagis.api.dto.request.organization.CreateOrganizationRequest;
import com.seffafbagis.api.dto.request.organization.ResubmitVerificationRequest;
import com.seffafbagis.api.dto.request.organization.UpdateOrganizationRequest;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.common.PageResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationDetailResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationListResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationSummaryResponse;
import com.seffafbagis.api.service.organization.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    // Public Endpoints

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrganizationListResponse>>> getAllOrganizations(
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<OrganizationListResponse> page;
        if (keyword != null && !keyword.trim().isEmpty()) {
            page = organizationService.searchOrganizations(keyword, pageable);
        } else {
            page = organizationService.getApprovedOrganizations(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(page)));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<OrganizationSummaryResponse>>> getFeaturedOrganizations() {
        List<OrganizationSummaryResponse> featured = organizationService.getFeaturedOrganizations();
        return ResponseEntity.ok(ApiResponse.success(featured));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationDetailResponse>> getOrganizationById(@PathVariable UUID id) {
        OrganizationDetailResponse response = organizationService.getOrganizationPublicDetail(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Foundation/Owner Endpoints

    @PostMapping
    @PreAuthorize("hasRole('FOUNDATION')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationResponse response = organizationService.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('FOUNDATION')")
    public ResponseEntity<ApiResponse<OrganizationDetailResponse>> getMyOrganization() {
        OrganizationDetailResponse response = organizationService.getMyOrganization();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('FOUNDATION')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateMyOrganization(
            @Valid @RequestBody UpdateOrganizationRequest request) {
        OrganizationResponse response = organizationService.updateOrganization(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/me/verify")
    @PreAuthorize("hasRole('FOUNDATION')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> submitForVerification() {
        OrganizationResponse response = organizationService.submitForVerification();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/me/resubmit")
    @PreAuthorize("hasRole('FOUNDATION')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> resubmitVerification(
            @Valid @RequestBody ResubmitVerificationRequest request) {
        OrganizationResponse response = organizationService.resubmitVerification(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
