package com.seffafbagis.api.service.interfaces;

import com.seffafbagis.api.dto.response.campaign.CampaignDetailResponse;
import com.seffafbagis.api.dto.response.campaign.CampaignResponse;
import com.seffafbagis.api.dto.response.campaign.CampaignStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for campaign operations.
 * Implementation will be provided by Emir.
 */
public interface ICampaignService {

    /**
     * Get campaign by ID
     * 
     * @param id Campaign UUID
     * @return CampaignResponse DTO
     * @throws com.seffafbagis.api.exception.ResourceNotFoundException if not found
     */
    CampaignResponse getById(UUID id);

    /**
     * Get campaign detail by ID
     *
     * @param id Campaign UUID
     * @return CampaignDetailResponse DTO
     */
    CampaignDetailResponse getCampaignDetail(UUID id);

    /**
     * Get all campaigns with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of CampaignResponse
     */
    Page<CampaignResponse> getAll(Pageable pageable);

    /**
     * Get campaigns pending approval
     * 
     * @param pageable Pagination parameters
     * @return Page of campaigns with status PENDING_APPROVAL
     */
    Page<CampaignResponse> getPendingApprovals(Pageable pageable);

    /**
     * Get campaigns by status
     * 
     * @param status   Campaign status
     * @param pageable Pagination parameters
     * @return Page of matching campaigns
     */
    Page<CampaignResponse> getByStatus(String status, Pageable pageable);

    /**
     * Update campaign approval status
     * 
     * @param id      Campaign ID
     * @param status  New status (ACTIVE, REJECTED)
     * @param reason  Reason for decision (required for rejection)
     * @param adminId Admin making the decision
     */
    void updateApprovalStatus(UUID id, String status, String reason, UUID adminId);

    /**
     * Get campaign statistics for dashboard
     * 
     * @return CampaignStatistics DTO
     */
    CampaignStatistics getStatistics();

    /**
     * Check if campaign exists
     * 
     * @param id Campaign ID
     * @return true if exists
     */
    boolean existsById(UUID id);

    /**
     * Get campaigns by organization
     * 
     * @param organizationId Organization ID
     * @param pageable       Pagination parameters
     * @return Page of campaigns for the organization
     */
    Page<CampaignResponse> getByOrganizationId(UUID organizationId, Pageable pageable);
}
