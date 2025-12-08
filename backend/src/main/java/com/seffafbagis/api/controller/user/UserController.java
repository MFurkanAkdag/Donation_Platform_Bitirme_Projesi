package com.seffafbagis.api.controller.user;

import com.seffafbagis.api.dto.request.user.UpdatePreferencesRequest;
import com.seffafbagis.api.dto.request.user.UpdateProfileRequest;
import com.seffafbagis.api.dto.response.ApiResponse;
import com.seffafbagis.api.dto.response.PagedResponse;
import com.seffafbagis.api.dto.response.user.UserResponse;
import com.seffafbagis.api.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Kullanıcı controller.
 * 
 * Kullanıcı bilgileri, profil ve tercih yönetimi.
 * 
 * Base URL: /api/v1/users
 * 
 * @author Furkan
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Kullanıcı işlemleri")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    /**
     * Constructor injection.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ==================== MEVCUT KULLANICI ====================

    /**
     * Mevcut kullanıcının bilgilerini döndürür.
     * 
     * GET /api/v1/users/me
     * 
     * @return UserResponse
     */
    @GetMapping("/me")
    @Operation(
        summary = "Mevcut kullanıcı bilgileri",
        description = "Giriş yapmış kullanıcının bilgilerini döndürür."
    )
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        logger.debug("Get current user endpoint called");

        UserResponse user = userService.getCurrentUser();

        ApiResponse<UserResponse> response = ApiResponse.success(user);
        return ResponseEntity.ok(response);
    }

    /**
     * Mevcut kullanıcının profilini günceller.
     * 
     * PATCH /api/v1/users/me/profile
     * 
     * @param request Profil güncelleme bilgileri
     * @return Güncellenmiş UserResponse
     */
    @PatchMapping("/me/profile")
    @Operation(
        summary = "Profil güncelleme",
        description = "Giriş yapmış kullanıcının profil bilgilerini günceller."
    )
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        logger.info("Update profile endpoint called");

        UserResponse user = userService.updateProfile(request);

        ApiResponse<UserResponse> response = ApiResponse.success(
            "Profil güncellendi",
            user
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Mevcut kullanıcının tercihlerini günceller.
     * 
     * PATCH /api/v1/users/me/preferences
     * 
     * @param request Tercih güncelleme bilgileri
     * @return Güncellenmiş UserResponse
     */
    @PatchMapping("/me/preferences")
    @Operation(
        summary = "Tercih güncelleme",
        description = "Giriş yapmış kullanıcının bildirim ve gizlilik tercihlerini günceller."
    )
    public ResponseEntity<ApiResponse<UserResponse>> updatePreferences(
            @Valid @RequestBody UpdatePreferencesRequest request) {

        logger.info("Update preferences endpoint called");

        UserResponse user = userService.updatePreferences(request);

        ApiResponse<UserResponse> response = ApiResponse.success(
            "Tercihler güncellendi",
            user
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Mevcut kullanıcının avatar'ını günceller.
     * 
     * PUT /api/v1/users/me/avatar
     * 
     * @param avatarUrl Avatar URL
     * @return Güncellenmiş UserResponse
     */
    @PutMapping("/me/avatar")
    @Operation(
        summary = "Avatar güncelleme",
        description = "Giriş yapmış kullanıcının profil fotoğrafını günceller."
    )
    public ResponseEntity<ApiResponse<UserResponse>> updateAvatar(
            @RequestParam String avatarUrl) {

        logger.info("Update avatar endpoint called");

        UserResponse user = userService.updateAvatar(avatarUrl);

        ApiResponse<UserResponse> response = ApiResponse.success(
            "Avatar güncellendi",
            user
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Mevcut kullanıcının avatar'ını siler.
     * 
     * DELETE /api/v1/users/me/avatar
     * 
     * @return Güncellenmiş UserResponse
     */
    @DeleteMapping("/me/avatar")
    @Operation(
        summary = "Avatar silme",
        description = "Giriş yapmış kullanıcının profil fotoğrafını siler."
    )
    public ResponseEntity<ApiResponse<UserResponse>> removeAvatar() {
        logger.info("Remove avatar endpoint called");

        UserResponse user = userService.removeAvatar();

        ApiResponse<UserResponse> response = ApiResponse.success(
            "Avatar silindi",
            user
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Mevcut kullanıcının hesabını deaktif eder.
     * 
     * DELETE /api/v1/users/me
     * 
     * @return Success response
     */
    @DeleteMapping("/me")
    @Operation(
        summary = "Hesap deaktif etme",
        description = "Giriş yapmış kullanıcının hesabını deaktif eder."
    )
    public ResponseEntity<ApiResponse<Void>> deactivateAccount() {
        logger.info("Deactivate account endpoint called");

        userService.deactivateAccount();

        ApiResponse<Void> response = ApiResponse.success(
            "Hesabınız deaktif edildi"
        );
        return ResponseEntity.ok(response);
    }

    // ==================== KULLANICI OKUMA ====================

    /**
     * ID ile kullanıcı bilgilerini döndürür.
     * 
     * GET /api/v1/users/{id}
     * 
     * @param id Kullanıcı ID
     * @return UserResponse
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Kullanıcı bilgileri",
        description = "ID ile kullanıcı bilgilerini döndürür."
    )
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        logger.debug("Get user by ID endpoint called: {}", id);

        UserResponse user = userService.getUserById(id);

        ApiResponse<UserResponse> response = ApiResponse.success(user);
        return ResponseEntity.ok(response);
    }

    // ==================== ADMİN İŞLEMLERİ ====================

    /**
     * Tüm kullanıcıları listeler.
     * Admin için.
     * 
     * GET /api/v1/users
     * 
     * @param pageable Sayfalama bilgisi
     * @return Sayfalanmış kullanıcı listesi
     */
    @GetMapping
    @Operation(
        summary = "Kullanıcı listesi (Admin)",
        description = "Tüm kullanıcıları sayfalama ile listeler. Admin yetkisi gerektirir."
    )
    public ResponseEntity<PagedResponse<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        logger.debug("Get all users endpoint called");

        Page<UserResponse> users = userService.getAllUsers(pageable);

        PagedResponse<UserResponse> response = PagedResponse.of(users, "Kullanıcılar listelendi");
        return ResponseEntity.ok(response);
    }

    /**
     * Kullanıcı arama.
     * Admin için.
     * 
     * GET /api/v1/users/search
     * 
     * @param q Arama terimi
     * @param pageable Sayfalama bilgisi
     * @return Sayfalanmış kullanıcı listesi
     */
    @GetMapping("/search")
    @Operation(
        summary = "Kullanıcı arama (Admin)",
        description = "E-posta adresine göre kullanıcı arar. Admin yetkisi gerektirir."
    )
    public ResponseEntity<PagedResponse<UserResponse>> searchUsers(
            @RequestParam String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        logger.debug("Search users endpoint called: {}", q);

        Page<UserResponse> users = userService.searchUsers(q, pageable);

        PagedResponse<UserResponse> response = PagedResponse.of(users, "Arama sonuçları");
        return ResponseEntity.ok(response);
    }

    /**
     * Kullanıcı hesabını askıya alır.
     * Admin için.
     * 
     * PATCH /api/v1/users/{id}/suspend
     * 
     * @param id Kullanıcı ID
     * @return Success response
     */
    @PatchMapping("/{id}/suspend")
    @Operation(
        summary = "Kullanıcı askıya alma (Admin)",
        description = "Kullanıcı hesabını askıya alır. Admin yetkisi gerektirir."
    )
    public ResponseEntity<ApiResponse<Void>> suspendUser(@PathVariable UUID id) {
        logger.info("Suspend user endpoint called: {}", id);

        userService.suspendUser(id);

        ApiResponse<Void> response = ApiResponse.success("Kullanıcı askıya alındı");
        return ResponseEntity.ok(response);
    }

    /**
     * Kullanıcı hesabını aktif eder.
     * Admin için.
     * 
     * PATCH /api/v1/users/{id}/activate
     * 
     * @param id Kullanıcı ID
     * @return Success response
     */
    @PatchMapping("/{id}/activate")
    @Operation(
        summary = "Kullanıcı aktif etme (Admin)",
        description = "Kullanıcı hesabını aktif eder. Admin yetkisi gerektirir."
    )
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable UUID id) {
        logger.info("Activate user endpoint called: {}", id);

        userService.activateUser(id);

        ApiResponse<Void> response = ApiResponse.success("Kullanıcı aktif edildi");
        return ResponseEntity.ok(response);
    }
}
