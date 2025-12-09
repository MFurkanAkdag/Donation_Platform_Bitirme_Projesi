package com.seffafbagis.api.controller.admin;

import com.seffafbagis.api.dto.request.admin.ApproveCampaignRequest;
import com.seffafbagis.api.dto.response.admin.AdminCampaignResponse;
import com.seffafbagis.api.dto.response.campaign.CampaignStatistics;
import com.seffafbagis.api.dto.response.common.PageResponse;
import com.seffafbagis.api.security.CustomUserDetails;
import com.seffafbagis.api.service.admin.AdminCampaignService;
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
@RequestMapping("/api/v1/admin/campaigns")
@Tag(name = "Admin - Campaigns", description = "Admin campaign management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCampaignController {

    private final AdminCampaignService adminCampaignService;

    public AdminCampaignController(AdminCampaignService adminCampaignService) {
        this.adminCampaignService = adminCampaignService;
    }

    @GetMapping
    @Operation(summary = "List all campaigns")
    public ResponseEntity<PageResponse<AdminCampaignResponse>> getAllCampaigns(Pageable pageable) {
        return ResponseEntity.ok(adminCampaignService.getAllCampaigns(pageable));
    }

    @GetMapping("/pending")
    @Operation(summary = "List pending approvals")
    public ResponseEntity<PageResponse<AdminCampaignResponse>> getPendingApprovals(Pageable pageable) {
        return ResponseEntity.ok(adminCampaignService.getPendingApprovals(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get campaign details")
    public ResponseEntity<AdminCampaignResponse> getCampaignById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminCampaignService.getCampaignById(id));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve or reject campaign")
    public ResponseEntity<AdminCampaignResponse> approveCampaign(
            @PathVariable UUID id,
            @Valid @RequestBody ApproveCampaignRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(adminCampaignService.approveCampaign(id, request, currentUser.getId()));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get campaign statistics")
    public ResponseEntity<CampaignStatistics> getCampaignStatistics() {
        return ResponseEntity.ok(adminCampaignService.getCampaignStatistics());
    }
}
