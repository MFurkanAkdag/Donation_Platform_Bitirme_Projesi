package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.donation.BankTransferReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for BankTransferReference entity.
 */
@Repository
public interface BankTransferReferenceRepository extends JpaRepository<BankTransferReference, UUID> {

    Optional<BankTransferReference> findByReferenceCode(String referenceCode);

    Optional<BankTransferReference> findByReferenceCodeAndStatus(String referenceCode, String status);

    Page<BankTransferReference> findAllByStatus(String status, Pageable pageable);

    Page<BankTransferReference> findByStatus(String status, Pageable pageable);

    @Query("SELECT b FROM BankTransferReference b WHERE b.status = 'pending' AND b.expiresAt < :now")
    List<BankTransferReference> findExpiredReferences(@Param("now") OffsetDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE BankTransferReference b SET b.status = 'expired' WHERE b.status = 'pending' AND b.expiresAt < :now")
    int expireOldReferences(@Param("now") OffsetDateTime now);

    List<BankTransferReference> findAllByDonorId(UUID donorId);

    List<BankTransferReference> findByDonorId(UUID donorId);

    List<BankTransferReference> findByStatusAndExpiresAtBefore(String status, OffsetDateTime expiresAt);

    boolean existsByReferenceCode(String referenceCode);

    List<BankTransferReference> findByStatus(String status);

    List<BankTransferReference> findByCampaignId(UUID campaignId);
}
