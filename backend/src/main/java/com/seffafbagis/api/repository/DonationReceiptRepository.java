package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.donation.DonationReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for DonationReceipt entity operations.
 */
@Repository
public interface DonationReceiptRepository extends JpaRepository<DonationReceipt, UUID> {

    Optional<DonationReceipt> findByDonationId(UUID donationId);

    Optional<DonationReceipt> findByReceiptNumber(String receiptNumber);

    boolean existsByDonationId(UUID donationId);

    @Query("SELECT MAX(CAST(SUBSTRING(r.receiptNumber, LENGTH(:prefix) + 1) AS int)) FROM DonationReceipt r WHERE r.receiptNumber LIKE :prefix")
    Integer findMaxReceiptNumberByYear(@Param("prefix") String prefix);
}
