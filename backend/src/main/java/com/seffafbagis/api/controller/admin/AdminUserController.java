package com.seffafbagis.api.controller.admin;

import com.seffafbagis.api.dto.request.admin.AdminCreateUserRequest;
import com.seffafbagis.api.dto.request.admin.UpdateUserRoleRequest;
import com.seffafbagis.api.dto.request.admin.UpdateUserStatusRequest;
import com.seffafbagis.api.dto.request.admin.UserSearchRequest;
import com.seffafbagis.api.dto.response.admin.AdminDashboardResponse;
import com.seffafbagis.api.dto.response.admin.AdminUserListResponse;
import com.seffafbagis.api.dto.response.admin.AdminUserResponse;
import com.seffafbagis.api.dto.response.admin.UserStatusChangeResponse;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.common.PageResponse;
import com.seffafbagis.api.security.CustomUserDetails;
import com.seffafbagis.api.service.admin.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AdminUserListResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getAllUsers(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<AdminUserListResponse>> searchUsers(
            @Valid UserSearchRequest request, Pageable pageable) {
        return ResponseEntity.ok(adminUserService.searchUsers(request, pageable));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminUserService.getUserById(userId));
    }

    @PostMapping
    public ResponseEntity<AdminUserResponse> createUser(
            @Valid @RequestBody AdminCreateUserRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminUserService.createUser(request, currentUser.getId()));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<UserStatusChangeResponse> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(adminUserService.updateUserStatus(userId, request, currentUser.getId()));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<Object>> updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(adminUserService.updateUserRole(userId, request, currentUser.getId()));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(adminUserService.deleteUser(userId, currentUser.getId()));
    }

    @PostMapping("/{userId}/unlock")
    public ResponseEntity<AdminUserResponse> unlockUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(adminUserService.unlockUser(userId, currentUser.getId()));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboardStatistics() {
        return ResponseEntity.ok(adminUserService.getDashboardStatistics());
    }

    @GetMapping("/{userId}/login-history")
    public ResponseEntity<PageResponse<Object>> getUserLoginHistory(
            @PathVariable UUID userId, Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getUserLoginHistory(userId, pageable));
    }
}
