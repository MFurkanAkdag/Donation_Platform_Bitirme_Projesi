package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.organization.OrganizationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for OrganizationDocument entity.
 */
@Repository
public interface OrganizationDocumentRepository extends JpaRepository<OrganizationDocument, UUID> {

    List<OrganizationDocument> findAllByOrganizationId(UUID organizationId);

    List<OrganizationDocument> findByOrganizationIdOrderByUploadedAtDesc(UUID organizationId);

    List<OrganizationDocument> findByOrganizationIdAndIsVerifiedFalse(UUID organizationId);

    List<OrganizationDocument> findAllByOrganizationIdAndDocumentType(UUID organizationId, String documentType);

    void deleteAllByOrganizationId(UUID organizationId);
}
