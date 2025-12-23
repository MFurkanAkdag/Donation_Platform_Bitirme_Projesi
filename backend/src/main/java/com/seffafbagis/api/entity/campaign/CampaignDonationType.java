package com.seffafbagis.api.entity.campaign;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.entity.category.DonationType;
import jakarta.persistence.*;

/**
 * Junction table for campaign-donationType many-to-many relationship.
 */
@Entity
@Table(name = "campaign_donation_types", indexes = {
        @Index(name = "idx_campaign_donation_types_campaign", columnList = "campaign_id"),
        @Index(name = "idx_campaign_donation_types_type", columnList = "donation_type_id")
})
public class CampaignDonationType extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_type_id", nullable = false)
    private DonationType donationType;

    // Getters and Setters

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public DonationType getDonationType() {
        return donationType;
    }

    public void setDonationType(DonationType donationType) {
        this.donationType = donationType;
    }
}
