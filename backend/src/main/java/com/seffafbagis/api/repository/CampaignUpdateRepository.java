package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.campaign.CampaignUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for CampaignUpdate entity.
 */
@Repository
public interface CampaignUpdateRepository extends JpaRepository<CampaignUpdate, UUID> {

    Page<CampaignUpdate> findAllByCampaignIdOrderByCreatedAtDesc(UUID campaignId, Pageable pageable);

    Page<CampaignUpdate> findByCampaignIdOrderByCreatedAtDesc(UUID campaignId, Pageable pageable);

    long countByCampaignId(UUID campaignId);

    void deleteAllByCampaignId(UUID campaignId);
}
