package com.seffafbagis.api.service.admin;

import com.seffafbagis.api.dto.request.admin.ApproveCampaignRequest;
import com.seffafbagis.api.dto.response.admin.AdminCampaignResponse;
import com.seffafbagis.api.dto.response.campaign.CampaignResponse;
import com.seffafbagis.api.dto.response.campaign.CampaignStatistics;
import com.seffafbagis.api.dto.response.common.PageResponse;
import com.seffafbagis.api.service.audit.AuditLogService;
import com.seffafbagis.api.service.interfaces.ICampaignService;
import com.seffafbagis.api.service.notification.EmailService;
import com.seffafbagis.api.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminCampaignService {

    private final ICampaignService campaignService;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    public AdminCampaignService(ICampaignService campaignService,
            EmailService emailService,
            AuditLogService auditLogService) {
        this.campaignService = campaignService;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
    }

    public PageResponse<AdminCampaignResponse> getAllCampaigns(Pageable pageable) {
        Page<CampaignResponse> campaigns = campaignService.getAll(pageable);
        List<AdminCampaignResponse> content = campaigns.getContent().stream()
                .map(this::mapToAdminResponse)
                .collect(Collectors.toList());

        return PageResponse.of(content, campaigns);
    }

    public PageResponse<AdminCampaignResponse> getPendingApprovals(Pageable pageable) {
        Page<CampaignResponse> campaigns = campaignService.getPendingApprovals(pageable);
        List<AdminCampaignResponse> content = campaigns.getContent().stream()
                .map(this::mapToAdminResponse)
                .collect(Collectors.toList());

        return PageResponse.of(content, campaigns);
    }

    public AdminCampaignResponse getCampaignById(UUID id) {
        CampaignResponse campaign = campaignService.getById(id);
        return mapToAdminResponse(campaign);
    }

    public AdminCampaignResponse approveCampaign(UUID id, ApproveCampaignRequest request, UUID adminId) {
        String decision = request.getDecision();
        String status = "APPROVE".equals(decision) ? "ACTIVE" : "REJECTED";

        if ("REJECTED".equals(status) && (request.getReason() == null || request.getReason().trim().isEmpty())) {
            throw new ValidationException("Reason is required for rejection");
        }

        campaignService.updateApprovalStatus(id, status, request.getReason(), adminId);

        // Audit Log
        String description = "Campaign " + (status.equals("ACTIVE") ? "approved" : "rejected") +
                ". Reason: " + request.getReason();
        auditLogService.logAction(adminId, "APPROVE_CAMPAIGN", description, id.toString());

        // Notify Organization logic would go here

        return getCampaignById(id);
    }

    public PageResponse<AdminCampaignResponse> getCampaignsByOrganization(UUID organizationId, Pageable pageable) {
        Page<CampaignResponse> campaigns = campaignService.getByOrganizationId(organizationId, pageable);
        List<AdminCampaignResponse> content = campaigns.getContent().stream()
                .map(this::mapToAdminResponse)
                .collect(Collectors.toList());

        return PageResponse.of(content, campaigns);
    }

    public CampaignStatistics getCampaignStatistics() {
        return campaignService.getStatistics();
    }

    private AdminCampaignResponse mapToAdminResponse(CampaignResponse campaign) {
        AdminCampaignResponse response = new AdminCampaignResponse();
        response.setId(campaign.getId());
        response.setOrganizationId(campaign.getOrganizationId());
        response.setTitle(campaign.getTitle());
        response.setDescription(campaign.getDescription());
        response.setStatus(campaign.getStatus());
        response.setRejectionReason(campaign.getRejectionReason());
        response.setTargetAmount(campaign.getTargetAmount());
        response.setCurrentAmount(campaign.getCurrentAmount());
        response.setApprovedAt(campaign.getApprovedAt());
        response.setCreatedAt(campaign.getCreatedAt());

        // Missing fields in placeholder: Organization Name, Dates, Scores etc.
        // Ideally we would fetch Organization name via IOrganizationService using
        // organizationId
        // But to keep it simple and within scope of parallel dev, we can leave it null
        // or implement retrieval if critical.

        return response;
    }
}
