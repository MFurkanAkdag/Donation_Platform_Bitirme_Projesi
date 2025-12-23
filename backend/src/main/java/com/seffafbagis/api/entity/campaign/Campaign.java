package com.seffafbagis.api.entity.campaign;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.enums.CampaignStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a fundraising campaign.
 */
@Entity
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

    // Getters and Setters

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getCollectedAmount() {
        return collectedAmount;
    }

    public void setCollectedAmount(BigDecimal collectedAmount) {
        this.collectedAmount = collectedAmount;
    }

    public Integer getDonorCount() {
        return donorCount;
    }

    public void setDonorCount(Integer donorCount) {
        this.donorCount = donorCount;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Boolean getIsUrgent() {
        return isUrgent;
    }

    public void setIsUrgent(Boolean isUrgent) {
        this.isUrgent = isUrgent;
    }

    public Integer getEvidenceDeadlineDays() {
        return evidenceDeadlineDays;
    }

    public void setEvidenceDeadlineDays(Integer evidenceDeadlineDays) {
        this.evidenceDeadlineDays = evidenceDeadlineDays;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public com.seffafbagis.api.entity.user.User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(com.seffafbagis.api.entity.user.User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public com.seffafbagis.api.entity.user.User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(com.seffafbagis.api.entity.user.User createdBy) {
        this.createdBy = createdBy;
    }
}
