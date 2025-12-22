package com.seffafbagis.api.entity.campaign;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignCategoryId implements Serializable {

    @Column(name = "campaign_id")
    private UUID campaignId;

    @Column(name = "category_id")
    private UUID categoryId;
}
