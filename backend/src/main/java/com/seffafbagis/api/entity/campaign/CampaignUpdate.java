package com.seffafbagis.api.entity.campaign;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.entity.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents updates/progress posts for a campaign.
 */
@Entity
@Table(name = "campaign_updates", indexes = {
        @Index(name = "idx_campaign_updates_campaign", columnList = "campaign_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class CampaignUpdate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;
}
