package com.seffafbagis.api.controller.campaign;

import com.seffafbagis.api.dto.response.campaign.CampaignResponse;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.service.campaign.CampaignFollowerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaign Followers", description = "Campaign following management")
public class CampaignFollowerController {

    private final CampaignFollowerService campaignFollowerService;

    @PostMapping("/{campaignId}/follow")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Follow campaign")
    public ApiResponse<Void> followCampaign(@PathVariable UUID campaignId) {
        campaignFollowerService.followCampaign(campaignId);
        return ApiResponse.success("You are now following this campaign");
    }

    @DeleteMapping("/{campaignId}/follow")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Unfollow campaign")
    public ApiResponse<Void> unfollowCampaign(@PathVariable UUID campaignId) {
        campaignFollowerService.unfollowCampaign(campaignId);
        return ApiResponse.success("You have unfollowed this campaign");
    }

    @GetMapping("/following")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get campaigns followed by current user")
    public ApiResponse<Page<CampaignResponse>> getFollowedCampaigns(Pageable pageable) {
        return ApiResponse.success(campaignFollowerService.getFollowedCampaigns(pageable));
    }
}
