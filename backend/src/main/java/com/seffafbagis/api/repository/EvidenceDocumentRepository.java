package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.evidence.EvidenceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for EvidenceDocument entity.
 */
@Repository
public interface EvidenceDocumentRepository extends JpaRepository<EvidenceDocument, UUID> {

    List<EvidenceDocument> findAllByEvidenceId(UUID evidenceId);

    List<EvidenceDocument> findByEvidenceId(UUID evidenceId);

    void deleteAllByEvidenceId(UUID evidenceId);
}
