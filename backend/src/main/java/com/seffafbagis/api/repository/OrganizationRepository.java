package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Organization entity.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByUserId(UUID userId);

    Optional<Organization> findByTaxNumber(String taxNumber);

    Page<Organization> findAllByVerificationStatus(VerificationStatus status, Pageable pageable);

    Page<Organization> findByVerificationStatus(VerificationStatus status, Pageable pageable);

    Page<Organization> findByVerificationStatusOrderByIsFeaturedDescCreatedAtDesc(VerificationStatus status,
            Pageable pageable);

    List<Organization> findByIsFeaturedTrueAndVerificationStatus(VerificationStatus status);

    @Query("SELECT o FROM Organization o WHERE " +
            "(LOWER(o.legalName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.displayName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "o.verificationStatus = :status")
    Page<Organization> searchByKeyword(@Param("keyword") String keyword,
            @Param("status") VerificationStatus status,
            Pageable pageable);

    boolean existsByTaxNumber(String taxNumber);

    long countByVerificationStatus(VerificationStatus status);
}
