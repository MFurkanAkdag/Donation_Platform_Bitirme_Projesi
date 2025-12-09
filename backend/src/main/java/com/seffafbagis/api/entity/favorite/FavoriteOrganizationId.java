package com.seffafbagis.api.entity.favorite;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite key class for FavoriteOrganization entity.
 * 
 * This class represents the composite primary key for the
 * favorite_organizations
 * junction table. It combines userId and organizationId to form a unique key.
 * 
 * The class implements Serializable as required by JPA for composite keys,
 * and properly overrides equals() and hashCode() for entity comparison.
 * 
 * @author Furkan
 * @since Phase 4
 */
@Embeddable
public class FavoriteOrganizationId implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The ID of the user who has favorited the organization.
     */
    private UUID userId;

    /**
     * The ID of the organization that has been favorited.
     */
    private UUID organizationId;

    /**
     * Default constructor required by JPA.
     */
    public FavoriteOrganizationId() {
    }

    /**
     * Creates a new composite key with the specified user and organization IDs.
     * 
     * @param userId         The ID of the user
     * @param organizationId The ID of the organization
     */
    public FavoriteOrganizationId(UUID userId, UUID organizationId) {
        this.userId = userId;
        this.organizationId = organizationId;
    }

    // ============================================
    // Getters and Setters
    // ============================================

    /**
     * Gets the user ID.
     * 
     * @return The user ID
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     * 
     * @param userId The user ID to set
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * Gets the organization ID.
     * 
     * @return The organization ID
     */
    public UUID getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the organization ID.
     * 
     * @param organizationId The organization ID to set
     */
    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    // ============================================
    // equals() and hashCode()
    // ============================================

    /**
     * Checks equality based on userId and organizationId.
     * Two FavoriteOrganizationId objects are equal if they have the same
     * userId and organizationId.
     * 
     * @param o The object to compare with
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FavoriteOrganizationId that = (FavoriteOrganizationId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(organizationId, that.organizationId);
    }

    /**
     * Generates hash code based on userId and organizationId.
     * 
     * @return The hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId, organizationId);
    }

    /**
     * Returns a string representation of the composite key.
     * 
     * @return String representation
     */
    @Override
    public String toString() {
        return "FavoriteOrganizationId{" +
                "userId=" + userId +
                ", organizationId=" + organizationId +
                '}';
    }
}
