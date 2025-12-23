package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.enums.DonationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Donation entity.
 */
@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {

        Page<Donation> findAllByDonorId(UUID donorId, Pageable pageable);

        Page<Donation> findByDonorId(UUID donorId, Pageable pageable);

        Page<Donation> findAllByCampaignId(UUID campaignId, Pageable pageable);

        Page<Donation> findByCampaignId(UUID campaignId, Pageable pageable);

        @Query("SELECT d FROM Donation d WHERE d.campaign.id = :campaignId AND d.isAnonymous = false AND d.status = :status")
        Page<Donation> findByCampaignIdAndIsAnonymousFalseAndStatus(
                        @Param("campaignId") UUID campaignId,
                        @Param("status") DonationStatus status,
                        Pageable pageable);

        @Query("SELECT d FROM Donation d WHERE d.campaign.organization.id = :organizationId")
        Page<Donation> findByCampaignOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

        List<Donation> findAllByCampaignIdAndStatus(UUID campaignId, DonationStatus status);

        @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = 'COMPLETED'")
        BigDecimal sumCompletedAmountByCampaignId(@Param("campaignId") UUID campaignId);

        @Query("SELECT COUNT(DISTINCT d.donor.id) FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = 'COMPLETED'")
        Long countUniqueDonorsByCampaignId(@Param("campaignId") UUID campaignId);

        long countByCampaignId(UUID campaignId);

        long countByDonorId(UUID donorId);

        long countByStatus(DonationStatus status);

        @Query("SELECT d FROM Donation d WHERE d.createdAt >= :startDate AND d.createdAt <= :endDate")
        List<Donation> findAllByDateRange(@Param("startDate") OffsetDateTime startDate,
                        @Param("endDate") OffsetDateTime endDate);

        @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = :status")
        BigDecimal sumAmountByCampaignIdAndStatus(@Param("campaignId") UUID campaignId,
                        @Param("status") DonationStatus status);

        List<Donation> findByCampaignIdAndStatus(UUID campaignId, DonationStatus status);

        // Better:
        @Query("SELECT d FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = :status")
        Page<Donation> findTopDonationsByCampaignId(@Param("campaignId") UUID campaignId,
                        @Param("status") DonationStatus status, Pageable pageable);

        @Query("SELECT COUNT(DISTINCT d.donor.id) FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = :status")
        long countDistinctDonorsByCampaignIdAndStatus(@Param("campaignId") UUID campaignId,
                        @Param("status") DonationStatus status);
}
