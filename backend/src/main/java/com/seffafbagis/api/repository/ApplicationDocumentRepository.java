package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.application.ApplicationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ApplicationDocument entity operations.
 */
@Repository
public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, UUID> {

    List<ApplicationDocument> findByApplicationId(UUID applicationId);

    void deleteByApplicationId(UUID applicationId);
}
