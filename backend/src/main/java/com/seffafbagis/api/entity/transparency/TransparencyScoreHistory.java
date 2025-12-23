package com.seffafbagis.api.entity.transparency;

import com.seffafbagis.api.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a change in an organization's transparency score.
 */
@Entity
@Table(name = "transparency_score_history", indexes = {
        @Index(name = "idx_transparency_history_org", columnList = "organization_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
