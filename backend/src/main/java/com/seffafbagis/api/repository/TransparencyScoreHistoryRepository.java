package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.transparency.TransparencyScoreHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for TransparencyScoreHistory entity.
 */
@Repository
public interface TransparencyScoreHistoryRepository extends JpaRepository<TransparencyScoreHistory, UUID> {

    Page<TransparencyScoreHistory> findAllByOrganizationIdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);

    List<TransparencyScoreHistory> findTop10ByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
