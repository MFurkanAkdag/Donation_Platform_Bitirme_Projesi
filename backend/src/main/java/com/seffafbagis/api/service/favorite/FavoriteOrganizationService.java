package com.seffafbagis.api.service.favorite;

import com.seffafbagis.api.dto.response.favorite.FavoriteCheckResponse;
import com.seffafbagis.api.dto.response.favorite.FavoriteOrganizationResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationResponse;
import com.seffafbagis.api.entity.favorite.FavoriteOrganization;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.FavoriteOrganizationRepository;
import com.seffafbagis.api.service.interfaces.IOrganizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FavoriteOrganizationService {

    private final FavoriteOrganizationRepository favoriteOrganizationRepository;
    private final IOrganizationService organizationService;

    public FavoriteOrganizationService(FavoriteOrganizationRepository favoriteOrganizationRepository,
            IOrganizationService organizationService) {
        this.favoriteOrganizationRepository = favoriteOrganizationRepository;
        this.organizationService = organizationService;
    }

    /**
     * Retrieves all favorite organizations for a user
     * @param userId The user ID
     * @return List of favorite organizations sorted by most recently favorited first
     */
    public List<FavoriteOrganizationResponse> getUserFavorites(UUID userId) {
        List<FavoriteOrganization> favorites = favoriteOrganizationRepository.findAllByUserId(userId);
        List<FavoriteOrganizationResponse> responses = new ArrayList<>();

        for (FavoriteOrganization fav : favorites) {
            try {
                OrganizationResponse org = organizationService.getById(fav.getOrganizationId());
                responses.add(buildFavoriteResponse(fav, org));
            } catch (ResourceNotFoundException e) {
                // If organization not found, we skip it to keep list clean
                // The favorite record can be cleaned up later via a scheduled task if needed
            }
        }

        return responses.stream()
                .sorted(Comparator.comparing(FavoriteOrganizationResponse::getFavoritedAt).reversed())
                .collect(Collectors.toList());
    }

    @Transactional
    public FavoriteOrganizationResponse addFavorite(UUID userId, UUID organizationId) {
        // Validate organization exists
        if (!organizationService.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization not found with id: " + organizationId);
        }

        // Check if already favorited - idempotent operation
        FavoriteOrganization existing = favoriteOrganizationRepository
                .findByUserIdAndOrganizationId(userId, organizationId)
                .orElse(null);

        if (existing != null) {
            // Already favorited, return existing (idempotent)
            OrganizationResponse org = organizationService.getById(organizationId);
            return buildFavoriteResponse(existing, org);
        }

        // Create new favorite
        FavoriteOrganization favorite = new FavoriteOrganization();
        favorite.setUserId(userId);
        favorite.setOrganizationId(organizationId);

        FavoriteOrganization saved = favoriteOrganizationRepository.save(favorite);
        OrganizationResponse org = organizationService.getById(organizationId);

        return buildFavoriteResponse(saved, org);
    }

    @Transactional
    public void removeFavorite(UUID userId, UUID organizationId) {
        favoriteOrganizationRepository.findByUserIdAndOrganizationId(userId, organizationId)
                .ifPresent(favoriteOrganizationRepository::delete);
    }

    public FavoriteCheckResponse isFavorited(UUID userId, UUID organizationId) {
        // We do not check if organization exists here to be lightweight
        return favoriteOrganizationRepository.findByUserIdAndOrganizationId(userId, organizationId)
                .map(fav -> new FavoriteCheckResponse(organizationId, true, fav.getCreatedAt()))
                .orElse(new FavoriteCheckResponse(organizationId, false, null));
    }

    public long getFavoriteCount(UUID organizationId) {
        return favoriteOrganizationRepository.countByOrganizationId(organizationId);
    }

    /**
     * Builds a FavoriteOrganizationResponse from a FavoriteOrganization entity and organization details
     * 
     * Note: OrganizationResponse currently provides basic information (id, name, description, verificationStatus).
     * For complete data (logo, activeCampaignsCount, totalRaised), wait for Organization module enhancement.
     * Using placeholder values (null, 0) for now to maintain API contract.
     * 
     * @param favorite The FavoriteOrganization entity
     * @param org The OrganizationResponse DTO
     * @return FavoriteOrganizationResponse with available data
     */
    private FavoriteOrganizationResponse buildFavoriteResponse(FavoriteOrganization favorite, 
            OrganizationResponse org) {
        return FavoriteOrganizationResponse.fromEntity(
                favorite,
                org.getId(),
                org.getName(),
                org.getLogo() != null ? org.getLogo() : null,           // Will be available in future
                org.getDescription(),
                org.getVerificationStatus(),
                org.getActiveCampaignsCount() != null ? org.getActiveCampaignsCount() : 0,  // Will be dynamic
                org.getTotalRaised() != null ? org.getTotalRaised() : BigDecimal.ZERO       // Will be dynamic
        );
    }
}
