package com.seffafbagis.api.entity.transparency;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.entity.organization.Organization;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an organization's transparency score.
 */
@Entity
@Table(name = "transparency_scores", indexes = {
        @Index(name = "idx_transparency_scores_org", columnList = "organization_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransparencyScore extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, unique = true)
    private Organization organization;

    @Column(name = "organization_id", insertable = false, updatable = false)
    private UUID organizationId;

    @Builder.Default
    @Column(name = "current_score", precision = 5, scale = 2)
    private BigDecimal currentScore = new BigDecimal("50.00");

    @Builder.Default
    @Column(name = "evidence_score", precision = 5, scale = 2)
    private BigDecimal evidenceScore = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "timeliness_score", precision = 5, scale = 2)
    private BigDecimal timelinessScore = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "report_score", precision = 5, scale = 2)
    private BigDecimal reportScore = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_campaigns")
    private Integer totalCampaigns = 0;

    @Builder.Default
    @Column(name = "completed_campaigns")
    private Integer completedCampaigns = 0;

    @Builder.Default
    @Column(name = "total_evidences")
    private Integer totalEvidences = 0;

    @Builder.Default
    @Column(name = "approved_evidences")
    private Integer approvedEvidences = 0;

    @Builder.Default
    @Column(name = "rejected_evidences")
    private Integer rejectedEvidences = 0;

    @Builder.Default
    @Column(name = "on_time_reports")
    private Integer onTimeReports = 0;

    @Builder.Default
    @Column(name = "late_reports")
    private Integer lateReports = 0;

    @Column(name = "last_calculated_at")
    private LocalDateTime lastCalculatedAt;
}
