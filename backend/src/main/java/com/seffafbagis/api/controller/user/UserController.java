package com.seffafbagis.api.controller.user;

import com.seffafbagis.api.dto.request.user.DeleteAccountRequest;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.user.UserDetailResponse;
import com.seffafbagis.api.dto.response.user.UserResponse;
import com.seffafbagis.api.security.CustomUserDetails;
import com.seffafbagis.api.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for general user operations.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user basic info")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse response = userService.getCurrentUser(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me/detail")
    @Operation(summary = "Get current user complete details")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getCurrentUserDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserDetailResponse response = userService.getCurrentUserDetail(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete current user account")
    public ResponseEntity<ApiResponse<String>> deleteAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DeleteAccountRequest request) {
        userService.deleteAccount(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
    }
}
