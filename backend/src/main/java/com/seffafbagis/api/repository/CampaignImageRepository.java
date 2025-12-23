package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.campaign.CampaignImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CampaignImage entity.
 */
@Repository
public interface CampaignImageRepository extends JpaRepository<CampaignImage, UUID> {

    List<CampaignImage> findAllByCampaignIdOrderBySortOrderAsc(UUID campaignId);

    List<CampaignImage> findByCampaignIdOrderByDisplayOrderAsc(UUID campaignId);

    Optional<CampaignImage> findByCampaignIdAndIsCoverTrue(UUID campaignId);

    long countByCampaignId(UUID campaignId);

    void deleteAllByCampaignId(UUID campaignId);
}
