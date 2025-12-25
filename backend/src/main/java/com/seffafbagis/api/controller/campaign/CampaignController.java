package com.seffafbagis.api.controller.campaign;

import com.seffafbagis.api.dto.request.campaign.CampaignSearchRequest;
import com.seffafbagis.api.dto.request.campaign.CreateCampaignRequest;
import com.seffafbagis.api.dto.request.campaign.UpdateCampaignRequest;
import com.seffafbagis.api.dto.request.campaign.UpdateRealizationRequest;
import com.seffafbagis.api.dto.response.campaign.CampaignDetailResponse;
import com.seffafbagis.api.dto.response.campaign.CampaignListResponse;
import com.seffafbagis.api.dto.response.campaign.CampaignResponse;
import com.seffafbagis.api.dto.response.campaign.CampaignStatsResponse;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.service.campaign.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaigns", description = "Campaign management endpoints")
public class CampaignController {

    private final CampaignService campaignService;

    // Public Endpoints

    @GetMapping
    @Operation(summary = "List active campaigns")
    public ApiResponse<Page<CampaignResponse>> getActiveCampaigns(Pageable pageable) {
        return ApiResponse.success(campaignService.getActiveCampaigns(pageable));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured campaigns")
    public ApiResponse<List<CampaignListResponse>> getFeaturedCampaigns() {
        return ApiResponse.success(campaignService.getFeaturedCampaigns());
    }

    @GetMapping("/urgent")
    @Operation(summary = "Get urgent campaigns")
    public ApiResponse<List<CampaignListResponse>> getUrgentCampaigns() {
        return ApiResponse.success(campaignService.getUrgentCampaigns());
    }

    @GetMapping("/category/{slug}")
    @Operation(summary = "Get campaigns by category")
    public ApiResponse<Page<CampaignResponse>> getCampaignsByCategory(@PathVariable String slug, Pageable pageable) {
        return ApiResponse.success(campaignService.getCampaignsByCategory(slug, pageable));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get campaign detail by slug")
    public ApiResponse<CampaignDetailResponse> getCampaignBySlug(@PathVariable String slug) {
        return ApiResponse.success(campaignService.getCampaignBySlug(slug));
    }

    @GetMapping("/search")
    @Operation(summary = "Search campaigns")
    public ApiResponse<Page<CampaignResponse>> searchCampaigns(@ModelAttribute CampaignSearchRequest request,
            Pageable pageable) {
        return ApiResponse.success(campaignService.searchCampaigns(request, pageable));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get campaigns by organization")
    public ApiResponse<Page<CampaignResponse>> getCampaignsByOrganization(@PathVariable UUID organizationId,
            Pageable pageable) {
        return ApiResponse.success(campaignService.getByOrganizationId(organizationId, pageable));
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Get campaign statistics")
    public ApiResponse<CampaignStatsResponse> getCampaignStats(@PathVariable UUID id) {
        return ApiResponse.success(campaignService.getCampaignStats(id));
    }

    // Owner Endpoints

    @GetMapping("/my")
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Get my campaigns")
    public ApiResponse<Page<CampaignResponse>> getMyCampaigns(Pageable pageable) {
        return ApiResponse.success(campaignService.getMyCampaigns(pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Create new campaign")
    public ApiResponse<CampaignResponse> createCampaign(@Valid @RequestBody CreateCampaignRequest request) {
        return ApiResponse.success("Campaign created successfully", campaignService.createCampaign(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Update campaign")
    public ApiResponse<CampaignDetailResponse> updateCampaign(@PathVariable UUID id,
            @Valid @RequestBody UpdateCampaignRequest request) {
        return ApiResponse.success("Campaign updated successfully", campaignService.updateCampaign(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Delete DRAFT campaign")
    public ApiResponse<Void> deleteCampaign(@PathVariable UUID id) {
        campaignService.deleteCampaign(id);
        return ApiResponse.success("Campaign deleted successfully");
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Submit campaign for approval")
    public ApiResponse<Void> submitForApproval(@PathVariable UUID id) {
        campaignService.submitForApproval(id);
        return ApiResponse.success("Campaign submitted for approval");
    }

    @PutMapping("/{id}/pause")
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Pause campaign")
    public ApiResponse<Void> pauseCampaign(@PathVariable UUID id) {
        campaignService.pauseCampaign(id);
        return ApiResponse.success("Campaign paused");
    }

    @PutMapping("/{id}/resume")
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Resume campaign")
    public ApiResponse<Void> resumeCampaign(@PathVariable UUID id) {
        campaignService.resumeCampaign(id);
        return ApiResponse.success("Campaign resumed");
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Complete campaign")
    public ApiResponse<Void> completeCampaign(@PathVariable UUID id) {
        campaignService.completeCampaign(id);
        return ApiResponse.success("Campaign completed");
    }

    @PutMapping("/{id}/realization")
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Update campaign realization status")
    public ApiResponse<Void> updateRealizationStatus(@PathVariable UUID id,
            @RequestBody UpdateRealizationRequest request) {
        campaignService.updateRealizationStatus(id, request.getStatus(), request.getDeadline());
        return ApiResponse.success("Realization status updated");
    }
}
