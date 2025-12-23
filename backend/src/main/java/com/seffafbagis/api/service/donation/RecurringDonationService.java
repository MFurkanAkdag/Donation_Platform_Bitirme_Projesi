package com.seffafbagis.api.service.donation;

import com.seffafbagis.api.dto.request.donation.CreateRecurringDonationRequest;
import com.seffafbagis.api.dto.request.donation.UpdateRecurringDonationRequest;
import com.seffafbagis.api.dto.response.donation.RecurringDonationListResponse;
import com.seffafbagis.api.dto.response.donation.RecurringDonationResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.category.DonationType;
import com.seffafbagis.api.entity.donation.RecurringDonation;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.DonationTypeRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.RecurringDonationRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringDonationService {

    private final RecurringDonationRepository recurringDonationRepository;
    private final CampaignRepository campaignRepository;
    private final OrganizationRepository organizationRepository;
    private final DonationTypeRepository donationTypeRepository;
    private final UserRepository userRepository;

    @Transactional
    public RecurringDonationResponse createRecurringDonation(CreateRecurringDonationRequest request) {
        User currentUser = getCurrentUser();

        RecurringDonation recurringDonation = new RecurringDonation();
        recurringDonation.setDonor(currentUser);
        recurringDonation.setAmount(request.getAmount());
        recurringDonation.setCurrency("TRY");
        recurringDonation.setFrequency(request.getFrequency());
        recurringDonation.setStatus("active");
        recurringDonation.setNextPaymentDate(calculateNextPaymentDate(LocalDate.now(), request.getFrequency()));
        recurringDonation.setTotalDonated(BigDecimal.ZERO);
        recurringDonation.setPaymentCount(0);
        recurringDonation.setFailureCount(0);

        if (request.getCampaignId() != null) {
            Campaign campaign = campaignRepository.findById(request.getCampaignId())
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
            recurringDonation.setCampaign(campaign);
            recurringDonation.setOrganization(campaign.getOrganization());
        } else if (request.getOrganizationId() != null) {
            Organization organization = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
            recurringDonation.setOrganization(organization);
        } else {
            throw new IllegalArgumentException("Either Campaign ID or Organization ID must be provided");
        }

        if (request.getDonationTypeId() != null) {
            DonationType donationType = donationTypeRepository.findById(request.getDonationTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Donation Type not found"));
            recurringDonation.setDonationType(donationType);
        }

        recurringDonation = recurringDonationRepository.save(recurringDonation);
        return mapToResponse(recurringDonation);
    }

    public RecurringDonationListResponse getMyRecurringDonations() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<RecurringDonation> donations = recurringDonationRepository.findByDonorId(userId);

        List<RecurringDonationResponse> items = donations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        BigDecimal totalMonthly = calculateTotalMonthlyAmount(donations);
        long totalActive = donations.stream().filter(d -> "active".equals(d.getStatus())).count();

        return RecurringDonationListResponse.builder()
                .items(items)
                .totalMonthlyAmount(totalMonthly)
                .totalActive(totalActive)
                .build();
    }

    public RecurringDonationResponse getRecurringDonation(UUID id) {
        RecurringDonation donation = getRecurringDonationEntity(id);
        checkOwnership(donation);
        return mapToResponse(donation);
    }

    @Transactional
    public RecurringDonationResponse updateRecurringDonation(UUID id, UpdateRecurringDonationRequest request) {
        RecurringDonation donation = getRecurringDonationEntity(id);
        checkOwnership(donation);

        if (request.getAmount() != null) {
            donation.setAmount(request.getAmount());
        }
        if (request.getFrequency() != null) {
            donation.setFrequency(request.getFrequency());
            // Optionally recalculate next payment date if frequency changes?
            // For now, let's keep the date and just apply new frequency for future updates.
            // Or better: Re-calculate from today if active?
            if ("active".equals(donation.getStatus())) {
                donation.setNextPaymentDate(calculateNextPaymentDate(LocalDate.now(), request.getFrequency()));
            }
        }
        if (request.getDonationTypeId() != null) {
            DonationType donationType = donationTypeRepository.findById(request.getDonationTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Donation Type not found"));
            donation.setDonationType(donationType);
        }

        donation = recurringDonationRepository.save(donation);
        return mapToResponse(donation);
    }

    @Transactional
    public void pauseRecurringDonation(UUID id) {
        RecurringDonation donation = getRecurringDonationEntity(id);
        checkOwnership(donation);
        donation.setStatus("paused");
        recurringDonationRepository.save(donation);
    }

    @Transactional
    public void resumeRecurringDonation(UUID id) {
        RecurringDonation donation = getRecurringDonationEntity(id);
        checkOwnership(donation);
        donation.setStatus("active");
        // Recalculate next payment date from today
        donation.setNextPaymentDate(calculateNextPaymentDate(LocalDate.now(), donation.getFrequency()));
        recurringDonationRepository.save(donation);
    }

    @Transactional
    public void cancelRecurringDonation(UUID id) {
        RecurringDonation donation = getRecurringDonationEntity(id);
        checkOwnership(donation);
        donation.setStatus("cancelled");
        recurringDonationRepository.save(donation);
    }

    // Internal methods for scheduler

    public List<RecurringDonation> getDueRecurringDonations(LocalDate date) {
        return recurringDonationRepository.findByStatusAndNextPaymentDateLessThanEqual("active", date);
    }

    @Transactional
    public void processRecurringPayment(UUID recurringDonationId) {
        RecurringDonation donation = recurringDonationRepository.findById(recurringDonationId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring Donation not found"));

        if (!"active".equals(donation.getStatus())) {
            log.warn("Skipping payment for inactive recurring donation: {}", recurringDonationId);
            return;
        }

        // This method prepares the donation for payment processing
        // Actual payment will be handled by Iyzico integration in Phase 9
        log.info("Processing recurring payment for subscription: {}, amount: {}",
                recurringDonationId, donation.getAmount());

        // TODO: Phase 9 - Call Iyzico payment service with saved card token
        // For now, just log the payment request
    }

    @Transactional
    public void handlePaymentFailure(UUID recurringDonationId, String error) {
        RecurringDonation donation = recurringDonationRepository.findById(recurringDonationId).orElse(null);
        if (donation != null) {
            donation.setFailureCount(donation.getFailureCount() + 1);
            if (donation.getFailureCount() >= 3) {
                donation.setStatus("paused");
                // TODO: Notify user
            }
            recurringDonationRepository.save(donation);
        }
    }

    @Transactional
    public void handlePaymentSuccess(UUID recurringDonationId, BigDecimal amount) {
        RecurringDonation donation = recurringDonationRepository.findById(recurringDonationId).orElse(null);
        if (donation != null) {
            donation.setLastPaymentDate(LocalDateTime.now().toLocalDate());
            donation.setTotalDonated(donation.getTotalDonated().add(donation.getAmount()));
            donation.setPaymentCount(donation.getPaymentCount() + 1);
            donation.setFailureCount(0);
            donation.setNextPaymentDate(calculateNextPaymentDate(LocalDate.now(), donation.getFrequency()));
            recurringDonationRepository.save(donation);
        }
    }

    private RecurringDonation getRecurringDonationEntity(UUID id) {
        return recurringDonationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring Donation not found"));
    }

    private User getCurrentUser() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void checkOwnership(RecurringDonation donation) {
        UUID currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!donation.getDonor().getId().equals(currentUserId)) {
            throw new com.seffafbagis.api.exception.AccessDeniedException("Not authorized");
        }
    }

    private LocalDate calculateNextPaymentDate(LocalDate fromDate, String frequency) {
        if ("weekly".equalsIgnoreCase(frequency)) {
            return fromDate.plusWeeks(1);
        } else if ("monthly".equalsIgnoreCase(frequency)) {
            return fromDate.plusMonths(1);
        } else if ("yearly".equalsIgnoreCase(frequency)) {
            return fromDate.plusYears(1);
        }
        return fromDate.plusMonths(1); // Default
    }

    private BigDecimal calculateTotalMonthlyAmount(List<RecurringDonation> donations) {
        BigDecimal total = BigDecimal.ZERO;
        for (RecurringDonation d : donations) {
            if ("active".equals(d.getStatus())) {
                BigDecimal monthlyEquivalent;
                if ("weekly".equalsIgnoreCase(d.getFrequency())) {
                    monthlyEquivalent = d.getAmount().multiply(BigDecimal.valueOf(4));
                } else if ("yearly".equalsIgnoreCase(d.getFrequency())) {
                    monthlyEquivalent = d.getAmount().divide(BigDecimal.valueOf(12), java.math.RoundingMode.HALF_UP);
                } else {
                    monthlyEquivalent = d.getAmount();
                }
                total = total.add(monthlyEquivalent);
            }
        }
        return total;
    }

    private RecurringDonationResponse mapToResponse(RecurringDonation donation) {
        String organizationName = null;
        if (donation.getOrganization() != null) {
            organizationName = donation.getOrganization().getLegalName();
        }

        String donationTypeCode = null;
        if (donation.getDonationType() != null) {
            donationTypeCode = donation.getDonationType().getTypeCode().name();
        }

        return RecurringDonationResponse.builder()
                .id(donation.getId())
                .campaignId(donation.getCampaign() != null ? donation.getCampaign().getId() : null)
                .campaignTitle(donation.getCampaign() != null ? donation.getCampaign().getTitle() : null)
                .organizationId(donation.getOrganization() != null ? donation.getOrganization().getId() : null)
                .organizationName(organizationName)
                .donationTypeCode(donationTypeCode)
                .amount(donation.getAmount())
                .currency(donation.getCurrency())
                .frequency(donation.getFrequency())
                .nextPaymentDate(donation.getNextPaymentDate())
                .lastPaymentDate(donation.getLastPaymentDate())
                .totalDonated(donation.getTotalDonated())
                .paymentCount(donation.getPaymentCount())
                .status(donation.getStatus())
                .createdAt(donation.getCreatedAt())
                .build();
    }
}
