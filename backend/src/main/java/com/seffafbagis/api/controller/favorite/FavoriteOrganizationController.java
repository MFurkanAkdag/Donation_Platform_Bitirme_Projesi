package com.seffafbagis.api.controller.favorite;

import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.favorite.FavoriteCheckResponse;
import com.seffafbagis.api.dto.response.favorite.FavoriteOrganizationResponse;
import com.seffafbagis.api.security.CustomUserDetails;
import com.seffafbagis.api.service.favorite.FavoriteOrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/favorites")
@Tag(name = "Favorites", description = "User favorite organizations")
public class FavoriteOrganizationController {

    private final FavoriteOrganizationService favoriteOrganizationService;

    public FavoriteOrganizationController(FavoriteOrganizationService favoriteOrganizationService) {
        this.favoriteOrganizationService = favoriteOrganizationService;
    }

    @GetMapping
    @Operation(summary = "Get favorite organizations")
    public ApiResponse<List<FavoriteOrganizationResponse>> getUserFavorites(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(favoriteOrganizationService.getUserFavorites(userDetails.getId()));
    }

    @PostMapping("/{organizationId}")
    @Operation(summary = "Add to favorites")
    public ResponseEntity<ApiResponse<FavoriteOrganizationResponse>> addFavorite(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        FavoriteOrganizationResponse response = favoriteOrganizationService.addFavorite(userDetails.getId(),
                organizationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/check/{organizationId}")
    @Operation(summary = "Check if favorited")
    public ApiResponse<FavoriteCheckResponse> checkFavorite(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(favoriteOrganizationService.isFavorited(userDetails.getId(), organizationId));
    }

    @DeleteMapping("/{organizationId}")
    @Operation(summary = "Remove from favorites")
    public ApiResponse<Void> removeFavorite(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        favoriteOrganizationService.removeFavorite(userDetails.getId(), organizationId);
        return ApiResponse.success("Organization removed from favorites");
    }
}
