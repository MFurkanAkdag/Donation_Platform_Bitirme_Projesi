package com.seffafbagis.api.service.organization;

import com.seffafbagis.api.dto.mapper.OrganizationMapper;
import com.seffafbagis.api.dto.request.admin.VerifyOrganizationRequest;
import com.seffafbagis.api.dto.request.organization.CreateOrganizationRequest;
import com.seffafbagis.api.dto.request.organization.ResubmitVerificationRequest;
import com.seffafbagis.api.dto.request.organization.UpdateOrganizationRequest;
import com.seffafbagis.api.dto.response.organization.*;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.VerificationStatus;
import com.seffafbagis.api.event.OrganizationRejectedEvent;
import com.seffafbagis.api.event.OrganizationVerifiedEvent;
import com.seffafbagis.api.exception.*;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.SecurityUtils;
import com.seffafbagis.api.service.audit.AuditLogService;
import com.seffafbagis.api.service.interfaces.IOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService implements IOrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final ApplicationEventPublisher eventPublisher;

    // ========== Public Methods ==========

    public Page<OrganizationListResponse> getApprovedOrganizations(Pageable pageable) {
        return organizationRepository.findByVerificationStatusOrderByIsFeaturedDescCreatedAtDesc(
                VerificationStatus.APPROVED, pageable)
                .map(organizationMapper::toListResponse);
    }

    public List<OrganizationSummaryResponse> getFeaturedOrganizations() {
        return organizationRepository.findByIsFeaturedTrueAndVerificationStatus(VerificationStatus.APPROVED)
                .stream()
                .map(organizationMapper::toSummaryResponse)
                .toList();
    }

    public Page<OrganizationListResponse> searchOrganizations(String keyword, Pageable pageable) {
        return organizationRepository.searchByKeyword(keyword, VerificationStatus.APPROVED, pageable)
                .map(organizationMapper::toListResponse);
    }

    public OrganizationDetailResponse getOrganizationPublicDetail(UUID id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        // If not approved, only owner or admin can see
        if (organization.getVerificationStatus() != VerificationStatus.APPROVED) {
            UUID currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            boolean isOwner = currentUserId != null && currentUserId.equals(organization.getUser().getId());
            boolean isAdmin = SecurityUtils.isAdmin();

            if (!isOwner && !isAdmin) {
                // If invalid status, treat as not found for public
                throw new ResourceNotFoundException("Organization not found");
            }
        }

        return organizationMapper.toDetailResponse(organization);
    }

    // ========== Owner Methods (FOUNDATION role) ==========

    public OrganizationDetailResponse getMyOrganization() {
        Organization organization = getCurrentUserOrganization();
        return organizationMapper.toDetailResponse(organization);
    }

    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        User user = getCurrentUser();

        // Check role
        if (!user.getRole().equals(UserRole.FOUNDATION)) {
            throw new ForbiddenException("Only FOUNDATION users can create organizations");
        }

        // Check if already has organization
        if (organizationRepository.findByUserId(user.getId()).isPresent()) {
            throw new ConflictException("User already has an organization");
        }

        // Check tax number uniqueness
        if (organizationRepository.existsByTaxNumber(request.getTaxNumber())) {
            throw new ConflictException("Tax number already in use");
        }

        Organization organization = organizationMapper.toEntity(request, user);
        organization = organizationRepository.save(organization);

        // Audit log
        auditLogService.logAction(user.getId(), "CREATE_ORGANIZATION",
                "Created organization: " + organization.getLegalName(), organization.getId().toString());

        return organizationMapper.toResponse(organization);
    }

    public OrganizationResponse updateOrganization(UpdateOrganizationRequest request) {
        Organization organization = getCurrentUserOrganization();

        // Cannot update distinct numbers if verified
        if (organization.getVerificationStatus() == VerificationStatus.APPROVED) {
            if (request.getTaxNumber() != null && !request.getTaxNumber().equals(organization.getTaxNumber())) {
                throw new BadRequestException("Cannot change tax number for verified organization");
            }
            // Add other critical fields logic if needed
        }

        // Check tax number uniqueness if changed
        if (request.getTaxNumber() != null && !request.getTaxNumber().equals(organization.getTaxNumber())) {
            if (organizationRepository.existsByTaxNumber(request.getTaxNumber())) {
                throw new ConflictException("Tax number already in use");
            }
        }

        organizationMapper.updateEntity(organization, request);
        organization = organizationRepository.save(organization);

        return organizationMapper.toResponse(organization);
    }

    public OrganizationResponse submitForVerification() {
        Organization organization = getCurrentUserOrganization();
        User user = organization.getUser();

        validateCanSubmitForVerification(organization);

        organization.setVerificationStatus(VerificationStatus.IN_REVIEW);
        organization = organizationRepository.save(organization);

        auditLogService.logAction(user.getId(), "SUBMIT_VERIFICATION",
                "Submitted for verification", organization.getId().toString());

        return organizationMapper.toResponse(organization);
    }

    public OrganizationResponse resubmitVerification(ResubmitVerificationRequest request) {
        Organization organization = getCurrentUserOrganization();
        User user = organization.getUser();

        validateCanResubmit(organization);

        organization.setVerificationStatus(VerificationStatus.IN_REVIEW);
        organization.setResubmissionCount(organization.getResubmissionCount() + 1);
        organization.setLastResubmissionAt(LocalDateTime.now());
        // Handle notes or audit info

        organization = organizationRepository.save(organization);

        auditLogService.logAction(user.getId(), "RESUBMIT_VERIFICATION",
                "Resubmitted organization. Notes: " + request.getAdditionalNotes(), organization.getId().toString());

        return organizationMapper.toResponse(organization);
    }

    // ========== IOrganizationService Implementation (Admin) ==========

    @Override
    public OrganizationResponse getById(UUID id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        return organizationMapper.toResponse(organization);
    }

    // This method is from the prompt interface, but I need to adapt if it's not in
    // the disk interface.
    // However, IOrganizationService on disk has DIFFERENT methods.
    // I need to implement methods from the DISK interface.

    @Override
    public Page<OrganizationResponse> getAll(Pageable pageable) {
        return organizationRepository.findAll(pageable)
                .map(organizationMapper::toResponse);
    }

    @Override
    public Page<OrganizationResponse> getPendingVerifications(Pageable pageable) {
        return organizationRepository.findByVerificationStatus(VerificationStatus.PENDING, pageable)
                .map(organizationMapper::toResponse);
    }

    @Override
    public Page<OrganizationResponse> getByVerificationStatus(String statusStr, Pageable pageable) {
        try {
            VerificationStatus status = VerificationStatus.valueOf(statusStr.toUpperCase());
            return organizationRepository.findByVerificationStatus(status, pageable)
                    .map(organizationMapper::toResponse);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid verification status: " + statusStr);
        }
    }

    @Override
    public void updateVerificationStatus(UUID id, String statusStr, String reason, UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        // Additional validation if needed (e.g. verify admin role, already verified at
        // controller level?)

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        VerificationStatus newStatus;
        try {
            newStatus = VerificationStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid verification status: " + statusStr);
        }

        if (newStatus == VerificationStatus.APPROVED) {
            // Verify
            organization.setVerificationStatus(VerificationStatus.APPROVED);
            organization.setVerifiedAt(LocalDateTime.now());
            organization.setVerifiedBy(admin);
            organization.setRejectionReason(null);
            organizationRepository.save(organization);

            // Publish organization verified event
            OrganizationVerifiedEvent verifiedEvent = new OrganizationVerifiedEvent(
                    adminId,
                    organization.getId(),
                    organization.getUser().getId());
            eventPublisher.publishEvent(verifiedEvent);
        } else if (newStatus == VerificationStatus.REJECTED) {
            // Reject
            if (reason == null || reason.trim().isEmpty()) {
                throw new BadRequestException("Reason is required for rejection");
            }
            organization.setVerificationStatus(VerificationStatus.REJECTED);
            organization.setRejectionReason(reason);
            organization.setVerifiedAt(null);
            organization.setVerifiedBy(admin); // Admins can be recorded on rejection too
            organizationRepository.save(organization);

            // Publish organization rejected event
            OrganizationRejectedEvent rejectedEvent = new OrganizationRejectedEvent(
                    adminId,
                    organization.getId(),
                    reason);
            eventPublisher.publishEvent(rejectedEvent);
        } else {
            organization.setVerificationStatus(newStatus);
            organizationRepository.save(organization);
        }
    }

    @Override
    public OrganizationStatistics getStatistics() {
        OrganizationStatistics stats = new OrganizationStatistics();
        stats.setTotalOrganizations(organizationRepository.count());
        stats.setPendingVerifications(organizationRepository.countByVerificationStatus(VerificationStatus.PENDING)); // PENDING
                                                                                                                     // +
                                                                                                                     // IN_REVIEW?
        // The prompt says "submitForVerification" sets status to IN_REVIEW. PENDING is
        // initial.
        // Usually, Admins verify IN_REVIEW items.
        // Prompt interface said getPendingVerifications.
        // Assuming "Pending" means waiting for action, which is IN_REVIEW (and maybe
        // PENDING if configured).

        // Re-checking "countByVerificationStatus".
        long inReview = organizationRepository.countByVerificationStatus(VerificationStatus.IN_REVIEW);
        long pending = organizationRepository.countByVerificationStatus(VerificationStatus.PENDING);

        stats.setPendingVerifications(inReview + pending); // Or just inReview? Usually Admin cares about submitted
                                                           // ones.

        stats.setVerifiedOrganizations(organizationRepository.countByVerificationStatus(VerificationStatus.APPROVED));
        stats.setRejectedOrganizations(organizationRepository.countByVerificationStatus(VerificationStatus.REJECTED));
        return stats;
    }

    @Override
    public boolean existsById(UUID id) {
        return organizationRepository.existsById(id);
    }

    // Additional methods for Prompt requirement (verify/reject)
    // These methods can be used by controller, or mapped to
    // updateVerificationStatus logic.
    // Note: AdminOrganizationService calls updateVerificationStatus directly.

    public void verify(UUID id, VerifyOrganizationRequest request) {
        // This is redundant if AdminService handles it, but creating for completeness
        // if requested.
        // Calling updateVerificationStatus
        UUID adminId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
        updateVerificationStatus(id, "APPROVED", request.getReason(), adminId);
    }

    public void reject(UUID id, VerifyOrganizationRequest request) {
        UUID adminId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
        updateVerificationStatus(id, "REJECTED", request.getReason(), adminId);
    }

    // ========== Helper Methods ==========

    private Organization getCurrentUserOrganization() {
        User user = getCurrentUser();
        return organizationRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User does not have an organization"));
    }

    private void validateCanSubmitForVerification(Organization org) {
        if (org.getVerificationStatus() == VerificationStatus.APPROVED) {
            throw new BadRequestException("Organization is already verified");
        }
        if (org.getVerificationStatus() == VerificationStatus.IN_REVIEW) {
            throw new BadRequestException("Organization is already in review");
        }

        // Check contacts
        if (org.getContacts() == null || org.getContacts().isEmpty()) {
            throw new BadRequestException("At least one contact is required");
        }

        // Check bank accounts
        if (org.getBankAccounts() == null || org.getBankAccounts().isEmpty()) {
            throw new BadRequestException("At least one bank account is required");
        }

        // Check documents (Assuming some documents are required)
        if (org.getDocuments() == null || org.getDocuments().isEmpty()) {
            throw new BadRequestException("Required documents are missing");
        }
    }

    private void validateCanResubmit(Organization org) {
        if (org.getVerificationStatus() != VerificationStatus.REJECTED) {
            throw new BadRequestException("Organization " + org.getVerificationStatus()
                    + " cannot be resubmitted. Only REJECTED organizations can request resubmission.");
        }

        if (org.getResubmissionCount() >= 3) {
            throw new BadRequestException("Maximum resubmission limit (3) reached. Please contact support.");
        }
    }

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not found")))
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    // Implementation of additional method from Prompt Interface that wasn't in File
    // Interface (if any)
    public Page<OrganizationResponse> getAllOrganizations(Pageable pageable) {
        return getAll(pageable);
    }

    public OrganizationDetailResponse getOrganizationDetail(UUID id) {
        return getOrganizationPublicDetail(id);
    }
}
