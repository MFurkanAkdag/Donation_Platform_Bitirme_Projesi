package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.transparency.TransparencyScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TransparencyScore entity.
 */
@Repository
public interface TransparencyScoreRepository extends JpaRepository<TransparencyScore, UUID> {

    Optional<TransparencyScore> findByOrganizationId(UUID organizationId);

    boolean existsByOrganizationId(UUID organizationId);
}
