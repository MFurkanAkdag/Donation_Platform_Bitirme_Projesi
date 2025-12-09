package com.seffafbagis.api.dto.response.favorite;

import java.time.LocalDateTime;
import java.util.UUID;

public class FavoriteCheckResponse {
    private UUID organizationId;
    private Boolean isFavorited;
    private LocalDateTime favoritedAt;

    public FavoriteCheckResponse(UUID organizationId, Boolean isFavorited, LocalDateTime favoritedAt) {
        this.organizationId = organizationId;
        this.isFavorited = isFavorited;
        this.favoritedAt = favoritedAt;
    }

    // Getters and Setters
    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public Boolean getIsFavorited() {
        return isFavorited;
    }

    public void setIsFavorited(Boolean isFavorited) {
        this.isFavorited = isFavorited;
    }

    public LocalDateTime getFavoritedAt() {
        return favoritedAt;
    }

    public void setFavoritedAt(LocalDateTime favoritedAt) {
        this.favoritedAt = favoritedAt;
    }
}
