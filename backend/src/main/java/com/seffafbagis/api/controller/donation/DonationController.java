package com.seffafbagis.api.controller.donation;

import com.seffafbagis.api.dto.request.donation.CreateDonationRequest;
import com.seffafbagis.api.dto.request.donation.RefundRequest;
import com.seffafbagis.api.dto.response.donation.DonationDetailResponse;
import com.seffafbagis.api.dto.response.donation.DonationReceiptResponse;
import com.seffafbagis.api.dto.response.donation.DonationResponse;
import com.seffafbagis.api.dto.response.donation.DonorListResponse;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.security.SecurityUtils;
import com.seffafbagis.api.service.donation.DonationReceiptService;
import com.seffafbagis.api.service.donation.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;
    private final DonationReceiptService donationReceiptService;
    private final OrganizationRepository organizationRepository;

    // Public donor list for a campaign
    @GetMapping("/campaign/{campaignId}/donors")
    public ResponseEntity<Page<DonorListResponse>> getCampaignDonors(
            @PathVariable UUID campaignId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(donationService.getCampaignDonors(campaignId, pageable));
    }

    // Authenticated User Endpoints
    @PostMapping
    @PreAuthorize("isAuthenticated() or isAnonymous()") // Allow anyone, but service handles logic
    // Actually prompt says "Authenticated User Endpoints: POST /api/v1/donations".
    // But also "Anonymous donations (without login) will be handled separately or
    // via a guest checkout flow."
    // And "Handling both authenticated and anonymous donations" in notes.
    // If I restrict to isAuthenticated(), then anonymous users can't hit it.
    // I'll allow all, but `DonationService` checks
    // `SecurityUtils.isAuthenticated()`.
    // Wait, prompt groups endpoints into "Authenticated User Endpoints".
    // I will stick to PreAuthorize("isAuthenticated()") for now per the grouping,
    // but looking at "Donation Creation: Users donate to campaigns (authenticated
    // or anonymous)"
    // it implies anonymous should be able to POST.
    // I'll make it publicly accessible but service handles user linkage if logged
    // in.
    public ResponseEntity<UUID> createDonation(@Valid @RequestBody CreateDonationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(donationService.createDonation(request));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<DonationResponse>> getMyDonations(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(donationService.getMyDonations(pageable));
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DonationDetailResponse> getMyDonationDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(donationService.getMyDonationDetail(id));
    }

    @GetMapping("/{id}/receipt")
    @PreAuthorize("isAuthenticated()")
    // Only owner should see receipt. Service gets receipt by donationId.
    // But I should check ownership.
    // `DonationReceiptService` currently doesn't check ownership.
    // I'll add ownership check here using `donationService`.
    public ResponseEntity<DonationReceiptResponse> getReceipt(@PathVariable UUID id) {
        // Reuse getMyDonationDetail to check ownership and get receipt
        DonationDetailResponse detail = donationService.getMyDonationDetail(id);
        if (detail.getReceipt() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail.getReceipt());
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> requestRefund(@PathVariable UUID id, @Valid @RequestBody RefundRequest request) {
        if (!id.equals(request.getDonationId())) {
            return ResponseEntity.badRequest().build();
        }
        donationService.requestRefund(request);
        return ResponseEntity.ok().build();
    }

    // Organization Endpoints
    @GetMapping("/organization")
    @PreAuthorize("hasAnyRole('FOUNDATION', 'ADMIN')")
    public ResponseEntity<Page<DonationResponse>> getOrganizationDonations(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UUID currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Organization organization = organizationRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found for current user"));

        return ResponseEntity.ok(donationService.getOrganizationDonations(organization.getId(), pageable));
    }

    @GetMapping("/campaign/{id}")
    @PreAuthorize("hasAnyRole('FOUNDATION', 'ADMIN')")
    public ResponseEntity<Page<DonationResponse>> getCampaignDonationsForOrg(
            @PathVariable UUID id,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(donationService.getCampaignDonations(id, pageable));
    }
}
