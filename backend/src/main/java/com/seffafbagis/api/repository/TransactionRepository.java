package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.donation.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Transaction entity.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByDonationId(UUID donationId);

    Optional<Transaction> findByTransactionId(String transactionId);

    Page<Transaction> findAllByStatus(String status, Pageable pageable);

    boolean existsByTransactionId(String transactionId);
}
