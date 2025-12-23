package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.campaign.CampaignDonationType;
import com.seffafbagis.api.entity.campaign.CampaignDonationTypeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for CampaignDonationType entity.
 */
@Repository
public interface CampaignDonationTypeRepository extends JpaRepository<CampaignDonationType, CampaignDonationTypeId> {

    List<CampaignDonationType> findAllByCampaignId(UUID campaignId);

    List<CampaignDonationType> findAllByDonationTypeId(UUID donationTypeId);

    boolean existsByCampaignIdAndDonationTypeId(UUID campaignId, UUID donationTypeId);

    void deleteAllByCampaignId(UUID campaignId);
}
