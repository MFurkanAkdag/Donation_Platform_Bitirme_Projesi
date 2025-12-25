package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByBarcodeData(String barcodeData);

    Optional<Receipt> findByDonationId(java.util.UUID donationId);
}
