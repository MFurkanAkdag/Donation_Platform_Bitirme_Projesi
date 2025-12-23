package com.seffafbagis.api.controller.campaign;

import com.seffafbagis.api.dto.request.campaign.AddCampaignImageRequest;
import com.seffafbagis.api.dto.response.campaign.CampaignImageResponse;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.service.campaign.CampaignImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campaigns/{campaignId}/images")
@RequiredArgsConstructor
@Tag(name = "Campaign Images", description = "Campaign gallery management")
public class CampaignImageController {

    private final CampaignImageService campaignImageService;

    @GetMapping
    @Operation(summary = "Get campaign images")
    public ApiResponse<List<CampaignImageResponse>> getImages(@PathVariable UUID campaignId) {
        return ApiResponse.success(campaignImageService.getImages(campaignId));
    }

    @PostMapping
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Add image to campaign")
    public ApiResponse<CampaignImageResponse> addImage(@PathVariable UUID campaignId,
            @Valid @RequestBody AddCampaignImageRequest request) {
        return ApiResponse.success("Image added successfully", campaignImageService.addImage(campaignId, request));
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Delete campaign image")
    public ApiResponse<Void> deleteImage(@PathVariable UUID campaignId, @PathVariable UUID imageId) {
        campaignImageService.deleteImage(imageId);
        return ApiResponse.success("Image deleted successfully");
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Reorder campaign images")
    public ApiResponse<Void> reorderImages(@PathVariable UUID campaignId, @RequestBody List<UUID> orderedImageIds) {
        campaignImageService.reorderImages(campaignId, orderedImageIds);
        return ApiResponse.success("Images reordered successfully");
    }
}
