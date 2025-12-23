package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.campaign.CampaignFollower;
import com.seffafbagis.api.entity.campaign.CampaignFollowerId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CampaignFollower entity.
 */
@Repository
public interface CampaignFollowerRepository extends JpaRepository<CampaignFollower, CampaignFollowerId> {

    Page<CampaignFollower> findAllByCampaignId(UUID campaignId, Pageable pageable);

    Page<CampaignFollower> findAllByUserId(UUID userId, Pageable pageable);

    List<CampaignFollower> findByUserId(UUID userId);

    List<CampaignFollower> findByCampaignId(UUID campaignId);

    Optional<CampaignFollower> findByCampaignIdAndUserId(UUID campaignId, UUID userId);

    boolean existsByCampaignIdAndUserId(UUID campaignId, UUID userId);

    long countByCampaignId(UUID campaignId);

    void deleteByCampaignIdAndUserId(UUID campaignId, UUID userId);
}
