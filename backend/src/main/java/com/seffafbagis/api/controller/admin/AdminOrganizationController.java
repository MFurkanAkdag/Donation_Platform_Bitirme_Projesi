package com.seffafbagis.api.controller.admin;

import com.seffafbagis.api.dto.request.admin.VerifyOrganizationRequest;
import com.seffafbagis.api.dto.response.admin.AdminCampaignResponse;
import com.seffafbagis.api.dto.response.admin.AdminOrganizationResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationStatistics;
import com.seffafbagis.api.dto.response.common.PageResponse;
import com.seffafbagis.api.security.CustomUserDetails;
import com.seffafbagis.api.service.admin.AdminCampaignService;
import com.seffafbagis.api.service.admin.AdminOrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/organizations")
@Tag(name = "Admin - Organizations", description = "Admin organization management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrganizationController {

    private final AdminOrganizationService adminOrganizationService;
    private final AdminCampaignService adminCampaignService;

    public AdminOrganizationController(AdminOrganizationService adminOrganizationService,
            AdminCampaignService adminCampaignService) {
        this.adminOrganizationService = adminOrganizationService;
        this.adminCampaignService = adminCampaignService;
    }

    @GetMapping
    @Operation(summary = "List all organizations")
    public ResponseEntity<PageResponse<AdminOrganizationResponse>> getAllOrganizations(Pageable pageable) {
        return ResponseEntity.ok(adminOrganizationService.getAllOrganizations(pageable));
    }

    @GetMapping("/pending")
    @Operation(summary = "List pending verifications")
    public ResponseEntity<PageResponse<AdminOrganizationResponse>> getPendingVerifications(Pageable pageable) {
        return ResponseEntity.ok(adminOrganizationService.getPendingVerifications(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization details")
    public ResponseEntity<AdminOrganizationResponse> getOrganizationById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminOrganizationService.getOrganizationById(id));
    }

    @PutMapping("/{id}/verify")
    @Operation(summary = "Verify or reject organization")
    public ResponseEntity<AdminOrganizationResponse> verifyOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody VerifyOrganizationRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(adminOrganizationService.verifyOrganization(id, request, currentUser.getId()));
    }

    @GetMapping("/{id}/campaigns")
    @Operation(summary = "Get organization's campaigns")
    public ResponseEntity<PageResponse<AdminCampaignResponse>> getOrganizationCampaigns(
            @PathVariable UUID id, Pageable pageable) {
        return ResponseEntity.ok(adminCampaignService.getCampaignsByOrganization(id, pageable));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get organization statistics")
    public ResponseEntity<OrganizationStatistics> getOrganizationStatistics() {
        return ResponseEntity.ok(adminOrganizationService.getOrganizationStatistics());
    }
}
