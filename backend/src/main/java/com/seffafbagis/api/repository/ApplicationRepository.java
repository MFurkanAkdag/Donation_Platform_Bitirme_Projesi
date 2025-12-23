package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.application.Application;
import com.seffafbagis.api.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Application (beneficiary request) entity.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID>, JpaSpecificationExecutor<Application> {

    Page<Application> findAllByCampaignId(UUID campaignId, Pageable pageable);

    Page<Application> findByStatus(ApplicationStatus status, Pageable pageable);

    List<Application> findByApplicantIdOrderByCreatedAtDesc(UUID applicantId);

    Page<Application> findByAssignedOrganizationId(UUID organizationId, Pageable pageable);

    List<Application> findAllByCampaignIdAndStatus(UUID campaignId, ApplicationStatus status);

    long countByCampaignId(UUID campaignId);

    long countByStatus(ApplicationStatus status);

    boolean existsByIdentityNumberAndCampaignId(String identityNumber, UUID campaignId);
}
