package com.seffafbagis.api.entity.campaign;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.entity.category.Category;
import jakarta.persistence.*;

import java.util.UUID;

/**
 * Junction table for campaign-category many-to-many relationship.
 */
@Entity
@Table(name = "campaign_categories", indexes = {
        @Index(name = "idx_campaign_categories_campaign", columnList = "campaign_id"),
        @Index(name = "idx_campaign_categories_category", columnList = "category_id")
})
public class CampaignCategory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Getters and Setters

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
