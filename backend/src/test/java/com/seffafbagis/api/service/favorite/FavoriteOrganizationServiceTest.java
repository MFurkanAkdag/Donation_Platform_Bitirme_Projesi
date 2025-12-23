package com.seffafbagis.api.service.favorite;

import com.seffafbagis.api.dto.response.favorite.FavoriteCheckResponse;
import com.seffafbagis.api.dto.response.favorite.FavoriteOrganizationResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationResponse;
import com.seffafbagis.api.entity.favorite.FavoriteOrganization;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.FavoriteOrganizationRepository;
import com.seffafbagis.api.service.interfaces.IOrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavoriteOrganizationServiceTest {

    @Mock
    private FavoriteOrganizationRepository favoriteOrganizationRepository;

    @Mock
    private IOrganizationService organizationService;

    private FavoriteOrganizationService favoriteOrganizationService;
    private UUID userId;
    private UUID organizationId;

    @BeforeEach
    void setUp() {
        favoriteOrganizationService = new FavoriteOrganizationService(
                favoriteOrganizationRepository, organizationService);
        userId = UUID.randomUUID();
        organizationId = UUID.randomUUID();
    }

    @Test
    void testGetUserFavorites_Success() {
        // Arrange
        FavoriteOrganization fav1 = new FavoriteOrganization();
        fav1.setId(UUID.randomUUID());
        fav1.setUserId(userId);
        fav1.setOrganizationId(organizationId);
        fav1.setCreatedAt(LocalDateTime.now().minusDays(1));

        UUID organizationId2 = UUID.randomUUID();
        FavoriteOrganization fav2 = new FavoriteOrganization();
        fav2.setId(UUID.randomUUID());
        fav2.setUserId(userId);
        fav2.setOrganizationId(organizationId2);
        fav2.setCreatedAt(LocalDateTime.now());

        when(favoriteOrganizationRepository.findAllByUserId(userId))
                .thenReturn(Arrays.asList(fav1, fav2));

        OrganizationResponse org1 = createOrganizationResponse(organizationId, "Org1");
        OrganizationResponse org2 = createOrganizationResponse(organizationId2, "Org2");

        when(organizationService.getById(organizationId)).thenReturn(org1);
        when(organizationService.getById(organizationId2)).thenReturn(org2);

        // Act
        List<FavoriteOrganizationResponse> result = favoriteOrganizationService.getUserFavorites(userId);

        // Assert
        assertEquals(2, result.size());
        // Should be sorted by most recent first
        assertEquals("Org2", result.get(0).getOrganizationName());
        assertEquals("Org1", result.get(1).getOrganizationName());
        verify(organizationService, times(2)).getById(any());
    }

    @Test
    void testGetUserFavorites_WithDeletedOrganization() {
        // Arrange
        FavoriteOrganization fav1 = new FavoriteOrganization();
        fav1.setId(UUID.randomUUID());
        fav1.setUserId(userId);
        fav1.setOrganizationId(organizationId);
        fav1.setCreatedAt(LocalDateTime.now());

        UUID organizationId2 = UUID.randomUUID();
        FavoriteOrganization fav2 = new FavoriteOrganization();
        fav2.setId(UUID.randomUUID());
        fav2.setUserId(userId);
        fav2.setOrganizationId(organizationId2);
        fav2.setCreatedAt(LocalDateTime.now());

        when(favoriteOrganizationRepository.findAllByUserId(userId))
                .thenReturn(Arrays.asList(fav1, fav2));

        // First organization exists
        OrganizationResponse org1 = createOrganizationResponse(organizationId, "Org1");
        when(organizationService.getById(organizationId)).thenReturn(org1);

        // Second organization deleted - throws exception
        when(organizationService.getById(organizationId2))
                .thenThrow(new ResourceNotFoundException("Organization not found"));

        // Act
        List<FavoriteOrganizationResponse> result = favoriteOrganizationService.getUserFavorites(userId);

        // Assert - Should return only one favorite (deleted org is skipped)
        assertEquals(1, result.size());
        assertEquals("Org1", result.get(0).getOrganizationName());
    }

    @Test
    void testAddFavorite_Success() {
        // Arrange
        when(organizationService.existsById(organizationId)).thenReturn(true);
        when(favoriteOrganizationRepository.findByUserIdAndOrganizationId(userId, organizationId))
                .thenReturn(Optional.empty());

        FavoriteOrganization newFavorite = new FavoriteOrganization();
        newFavorite.setId(UUID.randomUUID());
        newFavorite.setUserId(userId);
        newFavorite.setOrganizationId(organizationId);
        newFavorite.setCreatedAt(LocalDateTime.now());

        when(favoriteOrganizationRepository.save(any(FavoriteOrganization.class)))
                .thenReturn(newFavorite);

        OrganizationResponse org = createOrganizationResponse(organizationId, "Test Org");
        when(organizationService.getById(organizationId)).thenReturn(org);

        // Act
        FavoriteOrganizationResponse result = favoriteOrganizationService.addFavorite(userId, organizationId);

        // Assert
        assertNotNull(result);
        assertEquals("Test Org", result.getOrganizationName());
        assertEquals(organizationId, result.getOrganizationId());
        verify(favoriteOrganizationRepository).save(any(FavoriteOrganization.class));
    }

    @Test
    void testAddFavorite_AlreadyFavorited_Idempotent() {
        // Arrange
        when(organizationService.existsById(organizationId)).thenReturn(true);

        FavoriteOrganization existingFavorite = new FavoriteOrganization();
        existingFavorite.setId(UUID.randomUUID());
        existingFavorite.setUserId(userId);
        existingFavorite.setOrganizationId(organizationId);
        existingFavorite.setCreatedAt(LocalDateTime.now());

        when(favoriteOrganizationRepository.findByUserIdAndOrganizationId(userId, organizationId))
                .thenReturn(Optional.of(existingFavorite));

        OrganizationResponse org = createOrganizationResponse(organizationId, "Test Org");
        when(organizationService.getById(organizationId)).thenReturn(org);

        // Act
        FavoriteOrganizationResponse result = favoriteOrganizationService.addFavorite(userId, organizationId);

        // Assert
        assertNotNull(result);
        assertEquals("Test Org", result.getOrganizationName());
        // Should not save again (idempotent)
        verify(favoriteOrganizationRepository, never()).save(any(FavoriteOrganization.class));
    }

    @Test
    void testAddFavorite_OrganizationNotFound() {
        // Arrange
        when(organizationService.existsById(organizationId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            favoriteOrganizationService.addFavorite(userId, organizationId);
        });

        verify(favoriteOrganizationRepository, never()).save(any());
    }

    @Test
    void testRemoveFavorite_Success() {
        // Arrange
        FavoriteOrganization favorite = new FavoriteOrganization();
        favorite.setId(UUID.randomUUID());
        favorite.setUserId(userId);
        favorite.setOrganizationId(organizationId);

        when(favoriteOrganizationRepository.findByUserIdAndOrganizationId(userId, organizationId))
                .thenReturn(Optional.of(favorite));

        // Act
        assertDoesNotThrow(() -> {
            favoriteOrganizationService.removeFavorite(userId, organizationId);
        });

        // Assert
        verify(favoriteOrganizationRepository).delete(favorite);
    }

    @Test
    void testRemoveFavorite_NotFound_Idempotent() {
        // Arrange
        when(favoriteOrganizationRepository.findByUserIdAndOrganizationId(userId, organizationId))
                .thenReturn(Optional.empty());

        // Act
        assertDoesNotThrow(() -> {
            favoriteOrganizationService.removeFavorite(userId, organizationId);
        });

        // Assert - Should not throw error (idempotent)
        verify(favoriteOrganizationRepository, never()).delete(any());
    }

    @Test
    void testIsFavorited_True() {
        // Arrange
        FavoriteOrganization favorite = new FavoriteOrganization();
        favorite.setId(UUID.randomUUID());
        favorite.setCreatedAt(LocalDateTime.now());

        when(favoriteOrganizationRepository.findByUserIdAndOrganizationId(userId, organizationId))
                .thenReturn(Optional.of(favorite));

        // Act
        FavoriteCheckResponse result = favoriteOrganizationService.isFavorited(userId, organizationId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsFavorited());
        assertEquals(organizationId, result.getOrganizationId());
        assertNotNull(result.getFavoritedAt());
    }

    @Test
    void testIsFavorited_False() {
        // Arrange
        when(favoriteOrganizationRepository.findByUserIdAndOrganizationId(userId, organizationId))
                .thenReturn(Optional.empty());

        // Act
        FavoriteCheckResponse result = favoriteOrganizationService.isFavorited(userId, organizationId);

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsFavorited());
        assertEquals(organizationId, result.getOrganizationId());
        assertNull(result.getFavoritedAt());
    }

    @Test
    void testGetFavoriteCount() {
        // Arrange
        long expectedCount = 5L;
        when(favoriteOrganizationRepository.countByOrganizationId(organizationId))
                .thenReturn(expectedCount);

        // Act
        long result = favoriteOrganizationService.getFavoriteCount(organizationId);

        // Assert
        assertEquals(expectedCount, result);
        verify(favoriteOrganizationRepository).countByOrganizationId(organizationId);
    }

    // Helper method
    private OrganizationResponse createOrganizationResponse(UUID id, String name) {
        OrganizationResponse org = new OrganizationResponse();
        org.setId(id);
        org.setId(id);
        org.setLegalName(name);
        org.setDescription("Test description");
        org.setVerificationStatus(com.seffafbagis.api.enums.VerificationStatus.APPROVED);
        org.setLogoUrl("https://example.com/logo.png");
        org.setActiveCampaignsCount(3);
        org.setTotalRaised(new BigDecimal("10000.00"));
        return org;
    }
}
