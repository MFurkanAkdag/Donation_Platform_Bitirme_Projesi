package com.seffafbagis.api.entity.campaign;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.enums.CampaignStatus;

import jakarta.annotation.Generated;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a fundraising campaign.
 */
@Entity
@Getter
@Setter
@Table(name = "campaigns", indexes = {
        @Index(name = "idx_campaigns_slug", columnList = "slug", unique = true),
        @Index(name = "idx_campaigns_organization", columnList = "organization_id"),
        @Index(name = "idx_campaigns_status", columnList = "status")
})
public class Campaign extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 250)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "target_amount", precision = 14, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "collected_amount", precision = 14, scale = 2)
    private BigDecimal collectedAmount = BigDecimal.ZERO;

    @Column(name = "donor_count")
    private Integer donorCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_urgent")
    private Boolean isUrgent = false;

    @Column(name = "evidence_deadline_days")
    private Integer evidenceDeadlineDays = 15;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private com.seffafbagis.api.entity.user.User approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private com.seffafbagis.api.entity.user.User createdBy;

    @Column(name = "extension_count")
    private Integer extensionCount = 0;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "beneficiary_count")
    private Integer beneficiaryCount;

    @Column(name = "location_city")
    private String locationCity;

    @Column(name = "location_district")
    private String locationDistrict;

    @Column(length = 3)
    private String currency = "TRY";

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<CampaignCategory> categories;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<CampaignDonationType> donationTypes;

}
