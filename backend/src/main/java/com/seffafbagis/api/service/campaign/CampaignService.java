package com.seffafbagis.api.service.campaign;

import com.seffafbagis.api.dto.mapper.CampaignMapper;
import com.seffafbagis.api.dto.request.campaign.CampaignSearchRequest;
import com.seffafbagis.api.dto.request.campaign.CreateCampaignRequest;
import com.seffafbagis.api.dto.request.campaign.UpdateCampaignRequest;
import com.seffafbagis.api.dto.response.campaign.CampaignDetailResponse;
import com.seffafbagis.api.dto.response.campaign.CampaignListResponse;
import com.seffafbagis.api.dto.response.campaign.CampaignResponse;
import com.seffafbagis.api.dto.response.campaign.CampaignStatistics;
import com.seffafbagis.api.dto.response.campaign.CampaignStatsResponse;
import com.seffafbagis.api.entity.campaign.*;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.transparency.TransparencyScore;
import com.seffafbagis.api.enums.CampaignStatus;
import com.seffafbagis.api.enums.VerificationStatus;
import com.seffafbagis.api.event.CampaignApprovedEvent;
import com.seffafbagis.api.event.CampaignCompletedEvent;
import com.seffafbagis.api.event.CampaignCreatedEvent;
import com.seffafbagis.api.event.CampaignRejectedEvent;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ForbiddenException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.exception.UnauthorizedException;
import com.seffafbagis.api.repository.*;
import com.seffafbagis.api.security.SecurityUtils;
import com.seffafbagis.api.service.interfaces.ICampaignService;
import com.seffafbagis.api.service.transparency.TransparencyScoreService;
import com.seffafbagis.api.service.notification.NotificationService;
import com.seffafbagis.api.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CampaignService implements ICampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;
    private final OrganizationRepository organizationRepository;
    private final CategoryRepository categoryRepository;
    private final DonationTypeRepository donationTypeRepository;
    private final CampaignCategoryRepository campaignCategoryRepository;
    private final CampaignDonationTypeRepository campaignDonationTypeRepository;
    private final UserRepository userRepository;
    private final TransparencyScoreService transparencyScoreService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public CampaignResponse getById(UUID id) {
        Campaign campaign = findCampaignById(id);
        return campaignMapper.toResponse(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignDetailResponse getCampaignDetail(UUID id) {
        Campaign campaign = findCampaignById(id);
        return campaignMapper.toDetailResponse(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CampaignResponse> getAll(Pageable pageable) {
        return campaignRepository.findAll(pageable)
                .map(campaignMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CampaignResponse> getPendingApprovals(Pageable pageable) {
        return campaignRepository.findByStatus(CampaignStatus.PENDING_APPROVAL, pageable)
                .map(campaignMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CampaignResponse> getByStatus(String statusStr, Pageable pageable) {
        CampaignStatus status = CampaignStatus.valueOf(statusStr.toUpperCase());
        return campaignRepository.findByStatus(status, pageable)
                .map(campaignMapper::toResponse);
    }

    @Override
    public void updateApprovalStatus(UUID id, String statusStr, String reason, UUID adminId) {
        Campaign campaign = findCampaignById(id);
        CampaignStatus newStatus = CampaignStatus.valueOf(statusStr.toUpperCase());

        if (campaign.getStatus() != CampaignStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Campaign is not pending approval.");
        }

        if (newStatus == CampaignStatus.ACTIVE) {
            campaign.setStatus(CampaignStatus.ACTIVE);
            User admin = userRepository.findById(adminId)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
            campaign.setApprovedBy(admin);
            campaign.setApprovedAt(LocalDateTime.now());
            campaignRepository.save(campaign);

            // Publish campaign approved event
            CampaignApprovedEvent approvedEvent = new CampaignApprovedEvent(
                    adminId,
                    campaign.getId(),
                    campaign.getOrganization().getId(),
                    adminId);
            eventPublisher.publishEvent(approvedEvent);
        } else if (newStatus == CampaignStatus.DRAFT) {
            campaign.setStatus(CampaignStatus.DRAFT);
            campaignRepository.save(campaign);
        } else {
            campaign.setStatus(newStatus);
            campaignRepository.save(campaign);

            if (newStatus == CampaignStatus.REJECTED) {
                // Publish campaign rejected event
                CampaignRejectedEvent rejectedEvent = new CampaignRejectedEvent(
                        adminId,
                        campaign.getId(),
                        campaign.getOrganization().getId(),
                        reason);
                eventPublisher.publishEvent(rejectedEvent);
            }
        }
    }

    @Override
    public CampaignStatistics getStatistics() {
        return new CampaignStatistics();
    }

    @Override
    public boolean existsById(UUID id) {
        return campaignRepository.existsById(id);
    }

    @Override
    public Page<CampaignResponse> getByOrganizationId(UUID organizationId, Pageable pageable) {
        List<Campaign> campaigns = campaignRepository.findByOrganizationIdAndStatus(organizationId,
                CampaignStatus.ACTIVE);
        // FIXME: Repository should return Page. For now wrapping List in Page.
        // Note: This ignores pageable's limit/offset if repo returns proper list.
        // I will optimistically check if I can use findByOrganizationIdAndStatus with
        // Pageable if I add it to repo later.
        // Since I cannot change repo easily without fixing tests, I will wrap.
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), campaigns.size());
        List<Campaign> pageContent = (start <= end) ? campaigns.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, campaigns.size())
                .map(campaignMapper::toResponse);
    }

    public CampaignResponse createCampaign(CreateCampaignRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new UnauthorizedException("User not found"));
        Organization organization = organizationRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("User does not have an organization"));

        if (organization.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new ForbiddenException("Organization is not verified");
        }

        // Transparency Score Check
        // Allow if score is initialized (or handled in service) and sufficient
        // If service returns false, it means score exists and is low.
        // If score doesn't exist, we might want to initialize it first?
        // Let's ensure it's initialized for every org.
        transparencyScoreService.initializeScore(organization.getId());

        if (!transparencyScoreService.canCreateCampaign(organization.getId())) {
            throw new BadRequestException("Transparency score too low to create campaigns (Minimum 40 required)");
        }

        // Validation
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }

        Campaign campaign = campaignMapper.toEntity(request);
        campaign.setOrganization(organization);
        campaign.setStatus(CampaignStatus.DRAFT);
        campaign.setCreatedBy(userRepository.getReferenceById(userId)); // Use reference to avoid fetch

        String baseSlug = SlugGenerator.generateSlug(campaign.getTitle());
        String slug = baseSlug;
        int count = 1;
        while (campaignRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + count++;
        }
        campaign.setSlug(slug);

        Campaign savedCampaign = campaignRepository.save(campaign);

        if (request.getCategoryIds() != null) {
            for (UUID catId : request.getCategoryIds()) {
                boolean isPrimary = catId.equals(request.getPrimaryCategoryId());
                CampaignCategory cc = new CampaignCategory();
                cc.setCampaign(savedCampaign);
                cc.setCategory(categoryRepository.getReferenceById(catId));
                cc.setIsPrimary(isPrimary);
                campaignCategoryRepository.save(cc);
            }
        }

        if (request.getDonationTypeIds() != null) {
            for (UUID dtId : request.getDonationTypeIds()) {
                CampaignDonationType cdt = new CampaignDonationType();
                cdt.setId(new CampaignDonationTypeId(savedCampaign.getId(), dtId));
                cdt.setCampaign(savedCampaign);
                cdt.setDonationType(donationTypeRepository.getReferenceById(dtId));
                campaignDonationTypeRepository.save(cdt);
            }
        }

        // Publish campaign created event
        CampaignCreatedEvent createdEvent = new CampaignCreatedEvent(
                userId,
                savedCampaign.getId(),
                organization.getId(),
                savedCampaign.getTitle(),
                savedCampaign.getTargetAmount());
        eventPublisher.publishEvent(createdEvent);

        return campaignMapper.toResponse(savedCampaign);
    }

    public CampaignDetailResponse updateCampaign(UUID id, UpdateCampaignRequest request) {
        Campaign campaign = findCampaignById(id);
        verifyOwner(campaign);

        if (campaign.getStatus() == CampaignStatus.COMPLETED || campaign.getStatus() == CampaignStatus.CANCELLED) {
            throw new BadRequestException("Cannot update COMPLETED or CANCELLED campaign");
        }

        campaignMapper.updateEntity(campaign, request);
        return campaignMapper.toDetailResponse(campaignRepository.save(campaign));
    }

    public void deleteCampaign(UUID id) {
        Campaign campaign = findCampaignById(id);
        verifyOwner(campaign);

        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT campaigns can be deleted");
        }
        campaignRepository.delete(campaign);
    }

    public void submitForApproval(UUID id) {
        Campaign campaign = findCampaignById(id);
        verifyOwner(campaign);

        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT campaigns can be submitted");
        }
        campaign.setStatus(CampaignStatus.PENDING_APPROVAL);
        campaignRepository.save(campaign);
    }

    public void pauseCampaign(UUID id) {
        Campaign campaign = findCampaignById(id);
        verifyOwner(campaign);
        if (campaign.getStatus() != CampaignStatus.ACTIVE)
            throw new BadRequestException("Only ACTIVE campaigns can be paused");
        campaign.setStatus(CampaignStatus.PAUSED);
        campaignRepository.save(campaign);
    }

    public void resumeCampaign(UUID id) {
        Campaign campaign = findCampaignById(id);
        verifyOwner(campaign);
        if (campaign.getStatus() != CampaignStatus.PAUSED)
            throw new BadRequestException("Only PAUSED campaigns can be resumed");
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaignRepository.save(campaign);
    }

    public void completeCampaign(UUID id) {
        Campaign campaign = findCampaignById(id);
        verifyOwner(campaign);
        if (campaign.getStatus() != CampaignStatus.ACTIVE)
            throw new BadRequestException("Only ACTIVE campaigns can be completed");
        campaign.setStatus(CampaignStatus.COMPLETED);
        campaign.setCompletedAt(LocalDateTime.now());
        campaignRepository.save(campaign);

        // Publish campaign completed event
        UUID userId = SecurityUtils.getCurrentUserId().orElse(null);
        LocalDateTime evidenceDeadline = LocalDateTime.now().plusDays(30);
        int donorCount = campaign.getDonorCount() != null ? campaign.getDonorCount() : 0;
        BigDecimal collectedAmount = campaign.getCollectedAmount() != null ? campaign.getCollectedAmount()
                : BigDecimal.ZERO;

        CampaignCompletedEvent completedEvent = new CampaignCompletedEvent(
                userId,
                campaign.getId(),
                campaign.getOrganization().getId(),
                collectedAmount,
                donorCount,
                evidenceDeadline);
        eventPublisher.publishEvent(completedEvent);
    }

    @Transactional(readOnly = true)
    public CampaignDetailResponse getCampaignBySlug(String slug) {
        Campaign campaign = campaignRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with slug: " + slug));
        return campaignMapper.toDetailResponse(campaign);
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponse> searchCampaigns(CampaignSearchRequest request, Pageable pageable) {
        if (request.getKeyword() != null) {
            return campaignRepository.searchByKeyword(request.getKeyword(), CampaignStatus.ACTIVE, pageable)
                    .map(campaignMapper::toResponse);
        }
        return campaignRepository.findByStatus(CampaignStatus.ACTIVE, pageable)
                .map(campaignMapper::toResponse);
    }

    private Campaign findCampaignById(UUID id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
    }

    private void verifyOwner(Campaign campaign) {
        UUID currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not logged in"));
        Organization userOrg = organizationRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ForbiddenException("User has no organization"));

        if (!campaign.getOrganization().getId().equals(userOrg.getId())) {
            throw new ForbiddenException("You do not have permission to modify this campaign");
        }
    }

    public Page<CampaignResponse> getActiveCampaigns(Pageable pageable) {
        return campaignRepository.findByStatus(CampaignStatus.ACTIVE, pageable)
                .map(campaignMapper::toResponse);
    }

    // Public Methods - Featured & Urgent
    public List<CampaignListResponse> getFeaturedCampaigns() {
        return campaignRepository.findByIsFeaturedTrueAndStatus(CampaignStatus.ACTIVE)
                .stream()
                .map(campaignMapper::toListResponse)
                .collect(Collectors.toList());
    }

    public List<CampaignListResponse> getUrgentCampaigns() {
        return campaignRepository.findByIsUrgentTrueAndStatus(CampaignStatus.ACTIVE)
                .stream()
                .map(campaignMapper::toListResponse)
                .collect(Collectors.toList());
    }

    public Page<CampaignResponse> getCampaignsByCategory(String categorySlug, Pageable pageable) {
        return campaignRepository.findByCategorySlugAndStatus(categorySlug, CampaignStatus.ACTIVE, pageable)
                .map(campaignMapper::toResponse);
    }

    // Owner Methods
    public Page<CampaignResponse> getMyCampaigns(Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not logged in"));
        Organization org = organizationRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("User has no organization"));
        return campaignRepository.findByOrganizationId(org.getId(), pageable)
                .map(campaignMapper::toResponse);
    }

    public CampaignDetailResponse getMyCampaign(UUID id) {
        Campaign campaign = findCampaignById(id);
        verifyOwner(campaign);
        return campaignMapper.toDetailResponse(campaign);
    }

    // Statistics
    @Transactional(readOnly = true)
    public CampaignStatsResponse getCampaignStats(UUID campaignId) {
        Campaign campaign = findCampaignById(campaignId);
        CampaignStatsResponse stats = new CampaignStatsResponse();
        stats.setTotalDonations(
                campaign.getCollectedAmount() != null ? campaign.getCollectedAmount() : BigDecimal.ZERO);
        stats.setTotalDonors(campaign.getDonorCount() != null ? campaign.getDonorCount() : 0);
        if (stats.getTotalDonors() > 0) {
            stats.setAverageDonation(stats.getTotalDonations().divide(BigDecimal.valueOf(stats.getTotalDonors()), 2,
                    java.math.RoundingMode.HALF_UP));
        } else {
            stats.setAverageDonation(BigDecimal.ZERO);
        }
        return stats;
    }

    // Internal Methods for Donation Module
    @Transactional
    public void incrementDonationStats(UUID campaignId, BigDecimal amount) {
        Campaign campaign = findCampaignById(campaignId);
        BigDecimal current = campaign.getCollectedAmount() != null
                ? campaign.getCollectedAmount()
                : BigDecimal.ZERO;
        campaign.setCollectedAmount(current.add(amount));
        Integer currentDonors = campaign.getDonorCount() != null ? campaign.getDonorCount() : 0;
        campaign.setDonorCount(currentDonors + 1);
        campaignRepository.save(campaign);
        checkAndUpdateCompletionStatus(campaignId);
    }

    @Transactional
    public void checkAndUpdateCompletionStatus(UUID campaignId) {
        Campaign campaign = findCampaignById(campaignId);
        if (campaign.getStatus() == CampaignStatus.ACTIVE) {
            BigDecimal collected = campaign.getCollectedAmount() != null
                    ? campaign.getCollectedAmount()
                    : BigDecimal.ZERO;
            BigDecimal target = campaign.getTargetAmount();
            if (target != null && collected.compareTo(target) >= 0) {
                campaign.setStatus(CampaignStatus.COMPLETED);
                campaign.setCompletedAt(LocalDateTime.now());
                campaignRepository.save(campaign);

                try {
                    transparencyScoreService.onCampaignCompleted(campaignId);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }
}
