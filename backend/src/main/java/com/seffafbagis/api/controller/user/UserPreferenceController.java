package com.seffafbagis.api.controller.user;

import com.seffafbagis.api.dto.request.user.UpdatePreferencesRequest;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.user.UserPreferenceResponse;
import com.seffafbagis.api.security.CustomUserDetails;
import com.seffafbagis.api.service.user.UserPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for user preference management.
 */
@RestController
@RequestMapping("/api/v1/users/me/preferences")
@Tag(name = "User Preferences", description = "User preference management")
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    public UserPreferenceController(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }

    @GetMapping
    @Operation(summary = "Get current user preferences")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> getPreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserPreferenceResponse response = userPreferenceService.getPreferences(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    @Operation(summary = "Update current user preferences")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> updatePreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        UserPreferenceResponse response = userPreferenceService.updatePreferences(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Preferences updated successfully", response));
    }
}
