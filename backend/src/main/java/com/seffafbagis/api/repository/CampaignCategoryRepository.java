package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.campaign.CampaignCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for CampaignCategory entity.
 */
@Repository
public interface CampaignCategoryRepository extends JpaRepository<CampaignCategory, UUID> {

    List<CampaignCategory> findAllByCampaignId(UUID campaignId);

    List<CampaignCategory> findByCampaignId(UUID campaignId);

    List<CampaignCategory> findAllByCategoryId(UUID categoryId);

    boolean existsByCampaignIdAndCategoryId(UUID campaignId, UUID categoryId);

    void deleteAllByCampaignId(UUID campaignId);
}
