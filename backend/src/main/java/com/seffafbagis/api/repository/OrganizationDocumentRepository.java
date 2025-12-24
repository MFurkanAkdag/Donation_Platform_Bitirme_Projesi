package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.organization.OrganizationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
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

    List<OrganizationDocument> findByOrganizationIdAndDocumentType(UUID organizationId, String documentType);

    @org.springframework.data.jpa.repository.Query("SELECT d FROM OrganizationDocument d WHERE d.expiresAt < :date")
    List<OrganizationDocument> findExpiringDocuments(@Param("date") java.time.LocalDate date);

    void deleteAllByOrganizationId(UUID organizationId);
}
