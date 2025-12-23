package com.seffafbagis.api.entity.campaign;

import com.seffafbagis.api.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents images associated with a campaign.
 */
@Entity
@Table(name = "campaign_images", indexes = {
        @Index(name = "idx_campaign_images_campaign", columnList = "campaign_id"),
        @Index(name = "idx_campaign_images_cover", columnList = "campaign_id, is_cover")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class CampaignImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "caption")
    private String caption;

    @Column(name = "is_cover")
    private Boolean isCover = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "display_order")
    private Integer displayOrder = 0;
}
