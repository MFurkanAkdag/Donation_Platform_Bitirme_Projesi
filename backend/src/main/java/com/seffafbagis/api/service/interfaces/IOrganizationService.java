package com.seffafbagis.api.service.interfaces;

import com.seffafbagis.api.dto.response.organization.OrganizationResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for organization operations.
 * Implementation will be provided by Emir.
 */
public interface IOrganizationService {

    /**
     * Get organization by ID
     * 
     * @param id Organization UUID
     * @return OrganizationResponse DTO
     * @throws com.seffafbagis.api.exception.ResourceNotFoundException if not found
     */
    OrganizationResponse getById(UUID id);

    /**
     * Get all organizations with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of OrganizationResponse
     */
    Page<OrganizationResponse> getAll(Pageable pageable);

    /**
     * Get organizations pending verification
     * 
     * @param pageable Pagination parameters
     * @return Page of organizations with status PENDING
     */
    Page<OrganizationResponse> getPendingVerifications(Pageable pageable);

    /**
     * Get organizations by verification status
     * 
     * @param status   Verification status
     * @param pageable Pagination parameters
     * @return Page of matching organizations
     */
    Page<OrganizationResponse> getByVerificationStatus(String status, Pageable pageable);

    /**
     * Update organization verification status
     * 
     * @param id      Organization ID
     * @param status  New status (VERIFIED, REJECTED)
     * @param reason  Reason for decision (required for rejection)
     * @param adminId Admin making the decision
     */
    void updateVerificationStatus(UUID id, String status, String reason, UUID adminId);

    /**
     * Get organization statistics for dashboard
     * 
     * @return OrganizationStatistics DTO
     */
    OrganizationStatistics getStatistics();

    /**
     * Check if organization exists
     * 
     * @param id Organization ID
     * @return true if exists
     */
    boolean existsById(UUID id);
}
