package com.seffafbagis.api.controller.campaign;

import com.seffafbagis.api.dto.request.campaign.AddCampaignUpdateRequest;
import com.seffafbagis.api.dto.response.campaign.CampaignUpdateResponse;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.service.campaign.CampaignUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campaigns/{campaignId}/updates")
@RequiredArgsConstructor
@Tag(name = "Campaign Updates", description = "Campaign progress updates management")
public class CampaignUpdateController {

    private final CampaignUpdateService campaignUpdateService;

    @GetMapping
    @Operation(summary = "Get campaign updates")
    public ApiResponse<Page<CampaignUpdateResponse>> getUpdates(@PathVariable UUID campaignId, Pageable pageable) {
        return ApiResponse.success(campaignUpdateService.getUpdates(campaignId, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Add update to campaign")
    public ApiResponse<CampaignUpdateResponse> addUpdate(@PathVariable UUID campaignId,
            @Valid @RequestBody AddCampaignUpdateRequest request) {
        return ApiResponse.success("Update added successfully", campaignUpdateService.addUpdate(campaignId, request));
    }

    @DeleteMapping("/{updateId}")
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Delete campaign update")
    public ApiResponse<Void> deleteUpdate(@PathVariable UUID campaignId, @PathVariable UUID updateId) {
        campaignUpdateService.deleteUpdate(updateId);
        return ApiResponse.success("Update deleted successfully");
    }
}
