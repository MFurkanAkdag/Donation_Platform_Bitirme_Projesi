package com.seffafbagis.api.controller.system;

import com.seffafbagis.api.dto.request.system.CreateSettingRequest;
import com.seffafbagis.api.dto.request.system.UpdateSettingRequest;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.system.PublicSettingsResponse;
import com.seffafbagis.api.dto.response.system.SystemSettingResponse;
import com.seffafbagis.api.security.CustomUserDetails;
import com.seffafbagis.api.service.system.SystemSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "System Settings", description = "Platform configuration management")
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    public SystemSettingController(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    @GetMapping("/settings/public")
    @Operation(summary = "Get public settings", description = "Retrieve all publicly accessible settings. No authentication required. Results are cached for 1 hour.")
    @SecurityRequirement(name = "")  // Explicitly indicate no security requirement
    public ApiResponse<PublicSettingsResponse> getPublicSettings() {
        return ApiResponse.success(systemSettingService.getPublicSettings());
    }

    @GetMapping("/admin/settings")
    @Operation(summary = "Get all settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<SystemSettingResponse>> getAllSettings() {
        return ApiResponse.success(systemSettingService.getAllSettings());
    }

    @GetMapping("/admin/settings/{key}")
    @Operation(summary = "Get setting by key")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SystemSettingResponse> getSettingByKey(@PathVariable String key) {
        return ApiResponse.success(systemSettingService.getSettingByKey(key));
    }

    @PostMapping("/admin/settings")
    @Operation(summary = "Create new setting")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> createSetting(
            @Valid @RequestBody CreateSettingRequest request,
            @AuthenticationPrincipal CustomUserDetails adminDetails) {
        SystemSettingResponse response = systemSettingService.createSetting(request, adminDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/admin/settings/{key}")
    @Operation(summary = "Update setting")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SystemSettingResponse> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody UpdateSettingRequest request,
            @AuthenticationPrincipal CustomUserDetails adminDetails) {
        return ApiResponse.success(systemSettingService.updateSetting(key, request, adminDetails.getId()));
    }

    @DeleteMapping("/admin/settings/{key}")
    @Operation(summary = "Delete setting")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteSetting(@PathVariable String key) {
        systemSettingService.deleteSetting(key);
        return ApiResponse.success("Setting deleted successfully");
    }
}
