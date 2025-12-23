package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.donation.RecurringDonation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for RecurringDonation entity.
 */
@Repository
public interface RecurringDonationRepository extends JpaRepository<RecurringDonation, UUID> {

    Page<RecurringDonation> findAllByDonorId(UUID donorId, Pageable pageable);

    List<RecurringDonation> findByDonorId(UUID donorId);

    List<RecurringDonation> findAllByStatus(String status);

    List<RecurringDonation> findByStatusAndNextPaymentDateLessThanEqual(String status, LocalDate date);

    @Query("SELECT r FROM RecurringDonation r WHERE r.status = 'active' AND r.nextPaymentDate <= :date")
    List<RecurringDonation> findDueForProcessing(@Param("date") LocalDate date);

    @Query("SELECT r FROM RecurringDonation r WHERE r.campaign.id = :campaignId")
    List<RecurringDonation> findAllByCampaignId(@Param("campaignId") UUID campaignId);

    long countByDonorId(UUID donorId);

    boolean existsByDonorIdAndCampaignId(UUID donorId, UUID campaignId);
}
