package com.seffafbagis.api.service.evidence;

import com.seffafbagis.api.dto.mapper.EvidenceMapper;
import com.seffafbagis.api.dto.request.evidence.CreateEvidenceDocumentRequest;
import com.seffafbagis.api.dto.request.evidence.CreateEvidenceRequest;
import com.seffafbagis.api.dto.request.evidence.ReviewEvidenceRequest;
import com.seffafbagis.api.dto.request.evidence.UpdateEvidenceRequest;
import com.seffafbagis.api.dto.response.evidence.CampaignEvidenceSummaryResponse;
import com.seffafbagis.api.dto.response.evidence.EvidenceDetailResponse;
import com.seffafbagis.api.dto.response.evidence.EvidenceListResponse;
import com.seffafbagis.api.dto.response.evidence.EvidenceResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.evidence.Evidence;
import com.seffafbagis.api.entity.evidence.EvidenceDocument;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.CampaignStatus;
import com.seffafbagis.api.enums.EvidenceStatus;
import com.seffafbagis.api.event.EvidenceApprovedEvent;
import com.seffafbagis.api.event.EvidenceRejectedEvent;
import com.seffafbagis.api.event.EvidenceUploadedEvent;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ForbiddenException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.EvidenceRepository;
import com.seffafbagis.api.service.transparency.TransparencyScoreService;
import com.seffafbagis.api.service.user.UserService;
import com.seffafbagis.api.service.notification.NotificationService;
import com.seffafbagis.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final CampaignRepository campaignRepository;
    private final EvidenceDocumentService evidenceDocumentService;
    private final EvidenceMapper evidenceMapper;
    private final UserService userService;
    private final TransparencyScoreService transparencyScoreService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public EvidenceResponse uploadEvidence(CreateEvidenceRequest request) {
        User currentUser = userService.getUserById(
                SecurityUtils.getCurrentUserId().orElseThrow(() -> new ForbiddenException("Authentication required")));
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Campaign not found with id: " + request.getCampaignId()));

        validateOrganizationOwnership(campaign, currentUser);

        if (campaign.getStatus() != CampaignStatus.COMPLETED) {
            throw new BadRequestException("Evidence can only be uploaded for COMPLETED campaigns.");
        }

        Evidence evidence = evidenceMapper.toEntity(request, campaign, currentUser);
        evidence.setStatus(EvidenceStatus.PENDING);
        evidence.setUploadedAt(OffsetDateTime.now()); // Ensure this is set
        evidence = evidenceRepository.save(evidence);

        if (request.getDocuments() != null) {
            for (CreateEvidenceDocumentRequest docRequest : request.getDocuments()) {
                evidenceDocumentService.addDocument(evidence, docRequest);
            }
        }

        // Publish evidence uploaded event
        EvidenceUploadedEvent uploadedEvent = new EvidenceUploadedEvent(
                currentUser.getId(),
                evidence.getId(),
                campaign.getId(),
                campaign.getOrganization().getId(),
                evidence.getAmountSpent());
        eventPublisher.publishEvent(uploadedEvent);

        return evidenceMapper.toResponse(evidence);
    }

    @Transactional
    public EvidenceResponse updateEvidence(UUID id, UpdateEvidenceRequest request) {
        User currentUser = userService.getUserById(
                SecurityUtils.getCurrentUserId().orElseThrow(() -> new ForbiddenException("Authentication required")));
        Evidence evidence = evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found with id: " + id));

        validateOrganizationOwnership(evidence.getCampaign(), currentUser);

        if (evidence.getStatus() == EvidenceStatus.APPROVED) {
            throw new BadRequestException("Cannot update APPROVED evidence.");
        }
        // Rejected evidences can be updated/resubmitted

        if (request.getTitle() != null)
            evidence.setTitle(request.getTitle());
        if (request.getDescription() != null)
            evidence.setDescription(request.getDescription());
        if (request.getAmountSpent() != null)
            evidence.setAmountSpent(request.getAmountSpent());
        if (request.getSpendDate() != null)
            evidence.setSpendDate(request.getSpendDate());
        if (request.getVendorName() != null)
            evidence.setVendorName(request.getVendorName());
        if (request.getVendorTaxNumber() != null)
            evidence.setVendorTaxNumber(request.getVendorTaxNumber());
        if (request.getInvoiceNumber() != null)
            evidence.setInvoiceNumber(request.getInvoiceNumber());
        if (request.getEvidenceType() != null)
            evidence.setEvidenceType(request.getEvidenceType());

        // Reset status to PENDING if it was REJECTED
        if (evidence.getStatus() == EvidenceStatus.REJECTED) {
            evidence.setStatus(EvidenceStatus.PENDING);
            evidence.setRejectionReason(null);
            evidence.setReviewedBy(null);
            evidence.setReviewedAt(null);
        }

        evidence = evidenceRepository.save(evidence);

        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            for (CreateEvidenceDocumentRequest docRequest : request.getDocuments()) {
                evidenceDocumentService.addDocument(evidence, docRequest);
            }
        }

        return evidenceMapper.toResponse(evidence);
    }

    @Transactional
    public void deleteEvidence(UUID id) {
        User currentUser = userService.getUserById(
                SecurityUtils.getCurrentUserId().orElseThrow(() -> new ForbiddenException("Authentication required")));
        Evidence evidence = evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found with id: " + id));

        validateOrganizationOwnership(evidence.getCampaign(), currentUser);

        if (evidence.getStatus() != EvidenceStatus.PENDING && evidence.getStatus() != EvidenceStatus.REJECTED) {
            if (evidence.getStatus() != EvidenceStatus.PENDING) {
                throw new BadRequestException("Only PENDING evidence can be deleted.");
            }
        }

        evidenceRepository.delete(evidence);
    }

    public Page<EvidenceResponse> getMyEvidences(UUID campaignId, Pageable pageable) {
        User currentUser = userService.getUserById(
                SecurityUtils.getCurrentUserId().orElseThrow(() -> new ForbiddenException("Authentication required")));
        if (campaignId != null) {
            Campaign campaign = campaignRepository.findById(campaignId)
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));
            validateOrganizationOwnership(campaign, currentUser);
            return evidenceRepository.findByCampaignId(campaignId, pageable)
                    .map(evidenceMapper::toResponse);

        } else {
            return evidenceRepository.findByUploadedById(currentUser.getId(), pageable)
                    .map(evidenceMapper::toResponse);
        }
    }

    public EvidenceDetailResponse getEvidenceDetail(UUID id) {
        Evidence evidence = evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found with id: " + id));

        boolean isPublic = evidence.getStatus() == EvidenceStatus.APPROVED;

        if (!isPublic) {
            try {
                User currentUser = userService.getUserById(SecurityUtils.getCurrentUserId()
                        .orElseThrow(() -> new ForbiddenException("Authentication required")));
                boolean isOwner = evidence.getUploadedBy().getId().equals(currentUser.getId());
                boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

                if (!isOwner && !isAdmin) {
                    throw new ForbiddenException("You are not authorized to view this evidence.");
                }
            } catch (Exception e) {
                throw new ForbiddenException("You are not authorized to view this evidence.");
            }
        }

        return evidenceMapper.toDetailResponse(evidence);
    }

    // Admin methods
    public Page<EvidenceResponse> getPendingEvidences(Pageable pageable) {
        return evidenceRepository.findByStatus(EvidenceStatus.PENDING, pageable)
                .map(evidenceMapper::toResponse);
    }

    public Page<EvidenceResponse> getEvidencesByStatus(EvidenceStatus status, Pageable pageable) {
        return evidenceRepository.findByStatus(status, pageable)
                .map(evidenceMapper::toResponse);
    }

    @Transactional
    public EvidenceResponse reviewEvidence(UUID id, ReviewEvidenceRequest request) {
        Evidence evidence = evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found with id: " + id));

        if (evidence.getStatus() != EvidenceStatus.PENDING) {
            throw new BadRequestException("Only PENDING evidence can be reviewed.");
        }

        User reviewer = userService.getUserById(
                SecurityUtils.getCurrentUserId().orElseThrow(() -> new ForbiddenException("Authentication required")));

        if (request.getApproved()) {
            evidence.setStatus(EvidenceStatus.APPROVED);
            evidence.setRejectionReason(null);

            // Check if on time
            boolean onTime = !isDeadlineMissed(evidence.getCampaign().getId());
            Campaign campaign = evidence.getCampaign();
            if (campaign.getCompletedAt() != null) {
                LocalDateTime deadline = campaign.getCompletedAt().plusDays(
                        campaign.getEvidenceDeadlineDays() != null ? campaign.getEvidenceDeadlineDays() : 15);
                if (evidence.getUploadedAt() != null && evidence.getUploadedAt().toLocalDateTime().isAfter(deadline)) {
                    onTime = false;
                } else {
                    onTime = true;
                }
            } else {
                onTime = true; // Fallback
            }

            // Publish evidence approved event (listeners handle score and notifications)
            // Publish evidence approved event (listeners handle score and notifications)
            EvidenceApprovedEvent approvedEvent = new EvidenceApprovedEvent(
                    reviewer.getId(),
                    evidence.getId(),
                    evidence.getCampaign().getId(),
                    evidence.getCampaign().getOrganization().getId(),
                    evidence.getAmountSpent(),
                    onTime);
            eventPublisher.publishEvent(approvedEvent);

        } else {
            evidence.setStatus(EvidenceStatus.REJECTED);
            evidence.setRejectionReason(request.getRejectionReason());

            // Publish evidence rejected event (listeners handle score and notifications)
            EvidenceRejectedEvent rejectedEvent = new EvidenceRejectedEvent(
                    reviewer.getId(),
                    evidence.getId(),
                    evidence.getCampaign().getId(),
                    evidence.getCampaign().getOrganization().getId(),
                    request.getRejectionReason());
            eventPublisher.publishEvent(rejectedEvent);
        }

        evidence.setReviewedBy(reviewer);
        evidence.setReviewedAt(OffsetDateTime.now());

        evidence = evidenceRepository.save(evidence);
        return evidenceMapper.toResponse(evidence);
    }

    // Public methods
    public List<EvidenceResponse> getCampaignEvidences(UUID campaignId) {
        return evidenceRepository.findByCampaignIdAndStatus(campaignId, EvidenceStatus.APPROVED)
                .stream()
                .map(evidenceMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CampaignEvidenceSummaryResponse getCampaignEvidenceSummary(UUID campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));

        long totalEvidences = evidenceRepository.countByCampaignIdAndStatus(campaignId, EvidenceStatus.APPROVED) +
                evidenceRepository.countByCampaignIdAndStatus(campaignId, EvidenceStatus.PENDING) +
                evidenceRepository.countByCampaignIdAndStatus(campaignId, EvidenceStatus.REJECTED);

        long approvedCount = evidenceRepository.countByCampaignIdAndStatus(campaignId, EvidenceStatus.APPROVED);
        long pendingCount = evidenceRepository.countByCampaignIdAndStatus(campaignId, EvidenceStatus.PENDING);

        BigDecimal totalAmount = evidenceRepository.sumAmountSpentByCampaignIdAndStatus(campaignId,
                EvidenceStatus.APPROVED);

        return evidenceMapper.toSummaryResponse(campaign, totalEvidences, approvedCount, pendingCount, totalAmount);
    }

    // Internal methods (Phase 11 Support)
    public Map<String, Long> getEvidenceStats(UUID organizationId) {
        return Map.of();
    }

    public boolean isDeadlineMissed(UUID campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
        if (campaign == null || campaign.getCompletedAt() == null)
            return false;

        LocalDateTime deadline = campaign.getCompletedAt().plusDays(
                campaign.getEvidenceDeadlineDays() != null ? campaign.getEvidenceDeadlineDays() : 15);

        return LocalDateTime.now().isAfter(deadline);
    }

    @Transactional
    public void addDocumentToEvidence(UUID evidenceId, CreateEvidenceDocumentRequest request) {
        User currentUser = userService.getUserById(
                SecurityUtils.getCurrentUserId().orElseThrow(() -> new ForbiddenException("Authentication required")));
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found with id: " + evidenceId));

        validateOrganizationOwnership(evidence.getCampaign(), currentUser);

        if (evidence.getStatus() != EvidenceStatus.PENDING && evidence.getStatus() != EvidenceStatus.REJECTED) {
            throw new BadRequestException("Cannot add documents to approved evidence.");
        }

        evidenceDocumentService.addDocument(evidence, request);
    }

    @Transactional
    public void removeDocumentFromEvidence(UUID evidenceId, UUID documentId) {
        User currentUser = userService.getUserById(
                SecurityUtils.getCurrentUserId().orElseThrow(() -> new ForbiddenException("Authentication required")));
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found with id: " + evidenceId));

        validateOrganizationOwnership(evidence.getCampaign(), currentUser);

        if (evidence.getStatus() != EvidenceStatus.PENDING && evidence.getStatus() != EvidenceStatus.REJECTED) {
            throw new BadRequestException("Cannot remove documents from approved evidence.");
        }

        evidenceDocumentService.deleteDocument(documentId);
    }

    private void validateOrganizationOwnership(Campaign campaign, User user) {
        Organization organization = campaign.getOrganization();
        if (organization == null || organization.getUser() == null
                || !organization.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to manage evidence for this campaign.");
        }
    }
}
