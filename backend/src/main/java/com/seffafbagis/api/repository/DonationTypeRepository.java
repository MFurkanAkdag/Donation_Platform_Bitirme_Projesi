package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.category.DonationType;
import com.seffafbagis.api.enums.DonationTypeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for DonationType entity.
 */
@Repository
public interface DonationTypeRepository extends JpaRepository<DonationType, UUID> {

    Optional<DonationType> findByName(String name);

    Optional<DonationType> findByCode(String code);

    Optional<DonationType> findByTypeCode(DonationTypeCode typeCode);

    List<DonationType> findAllByIsActiveTrue();

    List<DonationType> findByIsActiveTrueOrderByNameAsc();

    boolean existsByCode(String code);
}
