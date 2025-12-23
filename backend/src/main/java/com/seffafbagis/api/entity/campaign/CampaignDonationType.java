package com.seffafbagis.api.entity.campaign;

import com.seffafbagis.api.entity.category.DonationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Junction table for campaign-donationType many-to-many relationship.
 */
@Entity
@Getter
@Setter
@Table(name = "campaign_donation_types", indexes = {
        @Index(name = "idx_campaign_donation_types_campaign", columnList = "campaign_id"),
        @Index(name = "idx_campaign_donation_types_type", columnList = "donation_type_id")
})
public class CampaignDonationType {

    @EmbeddedId
    private CampaignDonationTypeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("campaignId")
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("donationTypeId")
    @JoinColumn(name = "donation_type_id", nullable = false)
    private DonationType donationType;

}
