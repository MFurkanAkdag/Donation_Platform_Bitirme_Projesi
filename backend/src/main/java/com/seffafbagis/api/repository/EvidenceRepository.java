package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.evidence.Evidence;
import com.seffafbagis.api.enums.EvidenceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Evidence entity.
 */
@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, UUID> {

    Page<Evidence> findAllByCampaignId(UUID campaignId, Pageable pageable);

    Page<Evidence> findByCampaignId(UUID campaignId, Pageable pageable);

    List<Evidence> findAllByCampaignId(UUID campaignId);

    Page<Evidence> findAllByStatus(String status, Pageable pageable);

    Page<Evidence> findByStatus(EvidenceStatus status, Pageable pageable);

    Page<Evidence> findByUploadedById(UUID userId, Pageable pageable);

    List<Evidence> findByCampaignIdAndStatus(UUID campaignId, EvidenceStatus status);

    @Query("SELECT e FROM Evidence e WHERE e.campaign.organization.id = :orgId")
    Page<Evidence> findAllByOrganizationId(@Param("orgId") UUID organizationId, Pageable pageable);

    long countByCampaignIdAndStatus(UUID campaignId, String status);

    long countByCampaignIdAndStatus(UUID campaignId, EvidenceStatus status);

    @Query("SELECT COALESCE(SUM(e.amountSpent), 0) FROM Evidence e WHERE e.campaign.id = :campaignId AND e.status = :status")
    BigDecimal sumAmountSpentByCampaignIdAndStatus(@Param("campaignId") UUID campaignId,
            @Param("status") EvidenceStatus status);
}
