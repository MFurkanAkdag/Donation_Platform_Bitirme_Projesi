package com.seffafbagis.api.entity.transparency;

import com.seffafbagis.api.entity.base.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a change in an organization's transparency score.
 */
@Entity
@Table(name = "transparency_score_history", indexes = {
        @Index(name = "idx_transparency_history_org", columnList = "organization_id")
})
public class TransparencyScoreHistory extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "previous_score", precision = 5, scale = 2)
    private BigDecimal previousScore;

    @Column(name = "new_score", precision = 5, scale = 2)
    private BigDecimal newScore;

    @Column(name = "change_amount", precision = 5, scale = 2)
    private BigDecimal changeAmount;

    @Column(name = "change_reason", nullable = false, length = 50)
    private String changeReason;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Getters and Setters

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public BigDecimal getPreviousScore() {
        return previousScore;
    }

    public void setPreviousScore(BigDecimal previousScore) {
        this.previousScore = previousScore;
    }

    public BigDecimal getNewScore() {
        return newScore;
    }

    public void setNewScore(BigDecimal newScore) {
        this.newScore = newScore;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public UUID getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(UUID relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
