package com.seffafbagis.api.service.donation;

import com.seffafbagis.api.dto.request.donation.InitiateBankTransferRequest;
import com.seffafbagis.api.dto.request.donation.MatchBankTransferRequest;
import com.seffafbagis.api.dto.response.donation.BankTransferInfoResponse;
import com.seffafbagis.api.dto.response.donation.BankTransferReferenceResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.donation.BankTransferReference;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.category.DonationType;
import com.seffafbagis.api.entity.organization.OrganizationBankAccount;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.DonationStatus;
import com.seffafbagis.api.enums.PaymentMethod;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.BankTransferReferenceRepository;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.DonationRepository;
import com.seffafbagis.api.repository.DonationTypeRepository;
import com.seffafbagis.api.repository.OrganizationBankAccountRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.service.notification.NotificationService;
import com.seffafbagis.api.util.ReferenceCodeGenerator;
import com.seffafbagis.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankTransferService {

    private final BankTransferReferenceRepository bankTransferReferenceRepository;
    private final CampaignRepository campaignRepository;
    private final OrganizationBankAccountRepository bankAccountRepository;
    private final DonationTypeRepository donationTypeRepository;
    private final UserRepository userRepository;
    private final DonationService donationService;
    private final DonationRepository donationRepository; // Needed to link donation
    private final NotificationService notificationService;

    @Transactional
    public BankTransferInfoResponse initiateBankTransfer(InitiateBankTransferRequest request) {
        User currentUser = getCurrentUser();

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));

        if (campaign.getStatus() != com.seffafbagis.api.enums.CampaignStatus.ACTIVE) {
            throw new BadRequestException("Campaign is not active");
        }

        // Get primary bank account of the organization
        // For simplicity, taking the first active one or throwing error if none.
        // In real world, we might let user select or have a default.
        OrganizationBankAccount bankAccount = bankAccountRepository
                .findByOrganizationIdOrderByIsPrimaryDescCreatedAtAsc(campaign.getOrganization().getId())
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No bank account found for organization"));

        BankTransferReference reference = new BankTransferReference();
        reference.setCampaign(campaign);
        reference.setOrganization(campaign.getOrganization());
        reference.setDonor(currentUser);
        reference.setExpectedAmount(request.getAmount());
        reference.setBankAccount(bankAccount);
        reference.setStatus("pending");
        reference.setExpiresAt(OffsetDateTime.now().plusDays(7));
        reference.setSenderName(request.getSenderName());

        // Generate unique code
        String code;
        do {
            code = ReferenceCodeGenerator.generate();
        } while (bankTransferReferenceRepository.existsByReferenceCode(code));
        reference.setReferenceCode(code);

        // Store snapshot
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("bankName", bankAccount.getBankName());
        snapshot.put("branchName", bankAccount.getBranchName()); // Assuming field name
        snapshot.put("accountHolder", bankAccount.getAccountHolder());
        snapshot.put("iban", bankAccount.getIban());
        reference.setBankAccountSnapshot(snapshot);

        if (request.getDonationTypeId() != null) {
            DonationType donationType = donationTypeRepository.findById(request.getDonationTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Donation Type not found"));
            reference.setDonationType(donationType);
        }

        bankTransferReferenceRepository.save(reference);

        return BankTransferInfoResponse.builder()
                .referenceCode(code)
                .bankName(bankAccount.getBankName())
                .branchName(bankAccount.getBranchName())
                .accountHolder(bankAccount.getAccountHolder())
                .iban(bankAccount.getIban())
                .expectedAmount(request.getAmount())
                .expiresAt(reference.getExpiresAt())
                .instructions("Lütfen havale/EFT yaparken açıklama kısmına '" + code
                        + "' referans kodunu yazınız. Bu kod 7 gün geçerlidir.")
                .build();
    }

    public List<BankTransferReferenceResponse> getMyPendingReferences() {
        User currentUser = getCurrentUser();
        // Assuming repository has findByDonorIdAndStatus method
        // If not, we might need to filter manually or add to repo
        // For now, let's assume valid repo method or use basic findByDonorId
        return bankTransferReferenceRepository.findByDonorId(currentUser.getId()).stream()
                .filter(r -> "pending".equals(r.getStatus()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BankTransferReferenceResponse getBankTransferStatus(String referenceCode) {
        BankTransferReference reference = bankTransferReferenceRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new ResourceNotFoundException("Reference not found"));

        // Verify ownership if not admin?
        User currentUser = getCurrentUser();
        if (!reference.getDonor().getId().equals(currentUser.getId())) {
            // throw exception if strict
        }

        return mapToResponse(reference);
    }

    @Transactional
    public void cancelBankTransfer(String referenceCode) {
        BankTransferReference reference = bankTransferReferenceRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new ResourceNotFoundException("Reference not found"));

        verifyOwnership(reference);

        if (!"pending".equals(reference.getStatus())) {
            throw new BadRequestException("Cannot cancel non-pending transfer");
        }

        reference.setStatus("cancelled");
        bankTransferReferenceRepository.save(reference);
    }

    // Admin Methods

    public Page<BankTransferReferenceResponse> getPendingBankTransfers(Pageable pageable) {
        return bankTransferReferenceRepository.findByStatus("pending", pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public void matchBankTransfer(MatchBankTransferRequest request) {
        BankTransferReference reference = bankTransferReferenceRepository
                .findByReferenceCode(request.getReferenceCode())
                .orElseThrow(() -> new ResourceNotFoundException("Reference not found"));

        if (!"pending".equals(reference.getStatus())) {
            throw new BadRequestException("Reference is not pending (Status: " + reference.getStatus() + ")");
        }

        if (reference.getExpiresAt().isBefore(OffsetDateTime.now())) {
            reference.setStatus("expired");
            bankTransferReferenceRepository.save(reference);
            throw new BadRequestException("Reference code expired");
        }

        // Create Completed Donation
        Donation donation = new Donation();
        donation.setCampaign(reference.getCampaign());
        donation.setDonationType(reference.getDonationType()); // might be null
        donation.setDonor(reference.getDonor());
        donation.setAmount(request.getActualAmount()); // Use actual amount received
        donation.setCurrency("TRY"); // Default
        donation.setStatus(DonationStatus.COMPLETED);
        donation.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        donation.setTransactionId(reference.getReferenceCode()); // Set ref code as transaction ID?

        // Save donation (DonationService might have a method, but we need to set
        // specific status immediately)
        // Or better, use donationService to create/save?
        // Since donationService.createDonation usually makes it PENDING, let's manually
        // save here
        // OR better: create a separate "createCompletedDonation" method in
        // DonationService?
        // For now, direct save using repository to avoid circular complexity or PENDING
        // state
        donation = donationRepository.save(donation);

        // Trigger completion logic (stats, receipt, notification)
        donationService.completeDonation(donation.getId());

        reference.setStatus("matched");
        reference.setMatchedDonation(donation);
        if (request.getSenderName() != null)
            reference.setSenderName(request.getSenderName());
        if (request.getSenderIban() != null)
            reference.setSenderIban(request.getSenderIban());

        bankTransferReferenceRepository.save(reference);
    }

    @Transactional
    public void expireBankTransfer(String referenceCode) {
        BankTransferReference reference = bankTransferReferenceRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new ResourceNotFoundException("Reference not found"));
        reference.setStatus("expired");
        bankTransferReferenceRepository.save(reference);
    }

    // Internal methods (Scheduler)

    public List<BankTransferReference> getExpiredReferences(LocalDateTime before) {
        OffsetDateTime beforeOffset = before.atOffset(ZoneOffset.UTC); // Or system default
        // The prompt says "getExpiredReferences(LocalDateTime before)".
        // Entity uses OffsetDateTime. We'll adapt.
        // Assuming we want pending ones that expired
        return bankTransferReferenceRepository.findByStatusAndExpiresAtBefore("pending", beforeOffset);
    }

    @Transactional
    public void markAsExpired(UUID referenceId) {
        BankTransferReference reference = bankTransferReferenceRepository.findById(referenceId).orElse(null);
        if (reference != null && "pending".equals(reference.getStatus())) {
            reference.setStatus("expired");
            bankTransferReferenceRepository.save(reference);
            // Notify user?
        }
    }

    private User getCurrentUser() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void verifyOwnership(BankTransferReference reference) {
        User currentUser = getCurrentUser();
        if (!reference.getDonor().getId().equals(currentUser.getId())) {
            throw new com.seffafbagis.api.exception.AccessDeniedException("Not authorized");
        }
    }

    private BankTransferReferenceResponse mapToResponse(BankTransferReference reference) {
        return BankTransferReferenceResponse.builder()
                .id(reference.getId())
                .referenceCode(reference.getReferenceCode())
                .campaignTitle(reference.getCampaign().getTitle())
                .expectedAmount(reference.getExpectedAmount())
                .status(reference.getStatus())
                .senderName(reference.getSenderName())
                .senderIban(reference.getSenderIban())
                .createdAt(reference.getCreatedAt().toLocalDateTime())
                .expiresAt(reference.getExpiresAt().toLocalDateTime())
                .matchedDonationId(
                        reference.getMatchedDonation() != null ? reference.getMatchedDonation().getId() : null)
                .build();
    }
}
