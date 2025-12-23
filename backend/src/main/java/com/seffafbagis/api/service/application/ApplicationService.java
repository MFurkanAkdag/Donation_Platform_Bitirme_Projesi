package com.seffafbagis.api.service.application;

import com.seffafbagis.api.dto.mapper.ApplicationMapper;
import com.seffafbagis.api.dto.request.application.*;
import com.seffafbagis.api.dto.response.application.*;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.category.Category;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.application.Application;
import com.seffafbagis.api.enums.ApplicationStatus;
import com.seffafbagis.api.event.ApplicationStatusChangedEvent;
import com.seffafbagis.api.event.ApplicationSubmittedEvent;
import com.seffafbagis.api.repository.ApplicationRepository;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.CategoryRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;
    private final ApplicationDocumentService documentService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final OrganizationRepository organizationRepository;
    private final CampaignRepository campaignRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Applicant Methods

    @Transactional
    public ApplicationResponse createApplication(CreateApplicationRequest request) {
        User user = getCurrentUser();
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        Application application = applicationMapper.toEntity(request, user, category);

        application = applicationRepository.save(application);

        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            for (DocumentRequest docReq : request.getDocuments()) {
                documentService.addDocument(application, docReq);
            }
        }

        // Publish application submitted event
        ApplicationSubmittedEvent submittedEvent = new ApplicationSubmittedEvent(
                user.getId(),
                application.getId(),
                user.getId(),
                category.getName());
        eventPublisher.publishEvent(submittedEvent);

        return applicationMapper.toResponse(application);
    }

    public List<ApplicationResponse> getMyApplications() {
        User user = getCurrentUser();
        return applicationRepository.findByApplicantIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ApplicationDetailResponse getMyApplication(UUID id) {
        User user = getCurrentUser();
        Application application = getApplicationById(id);

        if (!application.getApplicant().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to view this application");
        }

        return applicationMapper.toDetailResponse(application);
    }

    @Transactional
    public ApplicationResponse updateApplication(UUID id, UpdateApplicationRequest request) {
        User user = getCurrentUser();
        Application application = getApplicationById(id);

        if (!application.getApplicant().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to update this application");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Only pending applications can be updated");
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            application.setCategory(category);
        }
        if (request.getTitle() != null)
            application.setTitle(request.getTitle());
        if (request.getDescription() != null)
            application.setDescription(request.getDescription());
        if (request.getRequestedAmount() != null)
            application.setRequestedAmount(request.getRequestedAmount());
        if (request.getLocationCity() != null)
            application.setLocationCity(request.getLocationCity());
        if (request.getLocationDistrict() != null)
            application.setLocationDistrict(request.getLocationDistrict());
        if (request.getHouseholdSize() != null)
            application.setHouseholdSize(request.getHouseholdSize());
        if (request.getUrgencyLevel() != null)
            application.setUrgencyLevel(request.getUrgencyLevel());

        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    @Transactional
    public void cancelApplication(UUID id) {
        User user = getCurrentUser();
        Application application = getApplicationById(id);

        if (!application.getApplicant().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to cancel this application");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Only pending applications can be cancelled");
        }

        applicationRepository.delete(application);
    }

    @Transactional
    public void addDocument(UUID applicationId, DocumentRequest request) {
        User user = getCurrentUser();
        Application application = getApplicationById(applicationId);

        if (!application.getApplicant().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to add documents to this application");
        }

        documentService.addDocument(application, request);
    }

    @Transactional
    public void removeDocument(UUID applicationId, UUID documentId) {
        User user = getCurrentUser();
        Application application = getApplicationById(applicationId);

        if (!application.getApplicant().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to remove documents from this application");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Documents can only be removed from pending applications");
        }

        documentService.removeDocument(documentId);
    }

    // Organization Methods

    public Page<ApplicationResponse> getAssignedApplications(Pageable pageable) {
        User user = getCurrentUser();
        Organization organization = organizationRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Organization not found for current user"));

        return applicationRepository.findByAssignedOrganizationId(organization.getId(), pageable)
                .map(applicationMapper::toResponse);
    }

    @Transactional
    public void assignToCampaign(UUID applicationId, AssignToCampaignRequest request) {
        User user = getCurrentUser();
        Organization organization = organizationRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        Application application = getApplicationById(applicationId);

        if (!organization.getId().equals(application.getAssignedOrganization().getId())) {
            throw new AccessDeniedException("This application is not assigned to your organization");
        }

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new IllegalStateException("Only approved applications can be assigned to a campaign");
        }

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found"));

        application.setAssignedCampaign(campaign);
        applicationRepository.save(application);
    }

    @Transactional
    public void completeApplication(UUID applicationId, CompleteApplicationRequest request) {
        User user = getCurrentUser();
        Organization organization = organizationRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        Application application = getApplicationById(applicationId);

        if (!organization.getId().equals(application.getAssignedOrganization().getId())) {
            throw new AccessDeniedException("This application is not assigned to your organization");
        }

        application.setStatus(ApplicationStatus.COMPLETED);
        applicationRepository.save(application);
    }

    // Admin Methods

    public Page<ApplicationResponse> getAllApplications(ApplicationStatus status, Pageable pageable) {
        if (status != null) {
            return applicationRepository.findByStatus(status, pageable)
                    .map(applicationMapper::toResponse);
        }
        return applicationRepository.findAll(pageable)
                .map(applicationMapper::toResponse);
    }

    public ApplicationDetailResponse getApplicationDetail(UUID id) {
        Application application = getApplicationById(id);
        return applicationMapper.toDetailResponse(application);
    }

    @Transactional
    public void reviewApplication(UUID id, ReviewApplicationRequest request) {
        User user = getCurrentUser();
        Application application = getApplicationById(id);

        ApplicationStatus previousStatus = application.getStatus();
        application.setStatus(request.getStatus());
        application.setReviewedBy(user);
        application.setReviewedAt(OffsetDateTime.now());

        if (request.getStatus() == ApplicationStatus.REJECTED) {
            String rejectionReason = request.getRejectionReason();
            if (rejectionReason != null && !rejectionReason.isEmpty()) {
                application.setDescription(application.getDescription() + "\n[REJECTION REASON]: " + rejectionReason);
            }
        }

        if (request.getAssignedOrganizationId() != null) {
            Organization org = organizationRepository.findById(request.getAssignedOrganizationId())
                    .orElseThrow(() -> new EntityNotFoundException("Organization not found"));
            application.setAssignedOrganization(org);
        }

        applicationRepository.save(application);

        // Publish application status changed event
        ApplicationStatusChangedEvent statusChangedEvent = new ApplicationStatusChangedEvent(
                user.getId(),
                application.getId(),
                application.getApplicant().getId(),
                previousStatus,
                request.getStatus());
        eventPublisher.publishEvent(statusChangedEvent);
    }

    @Transactional
    public void assignToOrganization(UUID applicationId, UUID organizationId) {
        Application application = getApplicationById(applicationId);
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        application.setAssignedOrganization(organization);
        applicationRepository.save(application);
    }

    public ApplicationStatsResponse getApplicationStats() {
        long total = applicationRepository.count();
        long pending = applicationRepository.countByStatus(ApplicationStatus.PENDING);
        long inReview = applicationRepository.countByStatus(ApplicationStatus.IN_REVIEW);
        long approved = applicationRepository.countByStatus(ApplicationStatus.APPROVED);
        long rejected = applicationRepository.countByStatus(ApplicationStatus.REJECTED);
        long completed = applicationRepository.countByStatus(ApplicationStatus.COMPLETED);

        return ApplicationStatsResponse.builder()
                .totalApplications(total)
                .pending(pending)
                .inReview(inReview)
                .approved(approved)
                .rejected(rejected)
                .completed(completed)
                .byCategory(new HashMap<>())
                .byCity(new HashMap<>())
                .build();
    }

    // Helpers

    public Application getApplicationById(UUID id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application not found with id: " + id));
    }

    private User getCurrentUser() {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
