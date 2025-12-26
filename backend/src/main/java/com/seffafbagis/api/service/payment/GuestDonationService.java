package com.seffafbagis.api.service.payment;

import com.seffafbagis.api.dto.request.payment.GuestCheckoutRequest;
import com.seffafbagis.api.dto.response.payment.GuestCheckoutResponse;
import com.seffafbagis.api.entity.Receipt;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.enums.DonationStatus;
import com.seffafbagis.api.enums.PaymentMethod;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.DonationRepository;
import com.seffafbagis.api.service.receipt.ReceiptService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling guest (anonymous) donations.
 * Allows users to donate without creating an account.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuestDonationService {

    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final ReceiptService receiptService;
    // TODO: Inject PaymentService when ready for actual payment processing

    /**
     * Process guest checkout: create donations, process payment, generate receipts
     *
     * @param request Guest checkout request with cart items and payment info
     * @return Response with created donations and receipts
     */
    @Transactional
    public GuestCheckoutResponse processGuestCheckout(GuestCheckoutRequest request) {
        log.info("Processing guest checkout for email: {}", request.getGuestEmail());

        // Validate all campaigns exist
        validateCampaigns(request.getCartItems());

        // Create donations for each cart item
        List<Donation> donations = createGuestDonations(request);

        // TODO: Process payment with payment gateway
        // For now, we'll mark donations as COMPLETED
        // In production, this should be PENDING until payment succeeds

        // Generate receipts for each donation
        List<GuestCheckoutResponse.DonationReceiptInfo> donationInfos = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Donation donation : donations) {
            // Generate receipt
            Receipt receipt = receiptService.createReceipt(donation);

            // Build response info
            Campaign campaign = donation.getCampaign();
            GuestCheckoutResponse.DonationReceiptInfo info = GuestCheckoutResponse.DonationReceiptInfo.builder()
                    .donationId(donation.getId())
                    .receiptId(UUID.fromString(receipt.getId().toString()))
                    .campaignId(campaign.getId())
                    .campaignTitle(campaign.getTitle())
                    .amount(donation.getAmount())
                    .currency(donation.getCurrency())
                    .receiptNumber(receipt.getBarcodeData())
                    .receiptPdfUrl("/api/v1/receipts/" + receipt.getId() + "/download")
                    .build();

            donationInfos.add(info);
            totalAmount = totalAmount.add(donation.getAmount());
        }

        log.info("Guest checkout completed. Created {} donations for total amount: {} TRY",
                donations.size(), totalAmount);

        return GuestCheckoutResponse.builder()
                .success(true)
                .message("Donation successful! Receipts have been sent to your email.")
                .donations(donationInfos)
                .totalAmount(totalAmount)
                .currency("TRY")
                .guestEmail(request.getGuestEmail())
                .build();
    }

    /**
     * Validate that all campaigns in cart items exist
     */
    private void validateCampaigns(List<GuestCheckoutRequest.CartItemRequest> cartItems) {
        for (GuestCheckoutRequest.CartItemRequest item : cartItems) {
            if (!campaignRepository.existsById(item.getCampaignId())) {
                throw new ResourceNotFoundException("Campaign not found with ID: " + item.getCampaignId());
            }
        }
    }

    /**
     * Create donation entities for guest user
     */
    private List<Donation> createGuestDonations(GuestCheckoutRequest request) {
        List<Donation> donations = new ArrayList<>();

        for (GuestCheckoutRequest.CartItemRequest item : request.getCartItems()) {
            Campaign campaign = campaignRepository.findById(item.getCampaignId())
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));

            Donation donation = new Donation();

            // Set campaign
            donation.setCampaign(campaign);

            // Guest user - donor is NULL
            donation.setDonor(null);

            // Amount and currency
            donation.setAmount(item.getAmount());
            donation.setCurrency(item.getCurrency());

            // Guest information stored in donation fields
            donation.setDonorDisplayName(request.getGuestName());
            donation.setIsAnonymous(request.getIsAnonymous());
            donation.setDonorMessage(request.getDonorMessage());

            // Payment info
            donation.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            donation.setStatus(DonationStatus.COMPLETED); // TODO: Change to PENDING until payment succeeds

            // Source tracking
            donation.setSource("web");

            // Save donation
            donation = donationRepository.save(donation);
            donations.add(donation);

            log.debug("Created guest donation: ID={}, Campaign={}, Amount={}",
                    donation.getId(), campaign.getTitle(), donation.getAmount());
        }

        return donations;
    }
}
