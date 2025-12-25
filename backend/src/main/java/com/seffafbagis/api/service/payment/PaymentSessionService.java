package com.seffafbagis.api.service.payment;

import com.seffafbagis.api.dto.request.payment.AddCartItemRequest;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.donation.PaymentSession;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.DonationStatus;
import com.seffafbagis.api.enums.PaymentSessionStatus;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.DonationRepository;
import com.seffafbagis.api.repository.PaymentSessionRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.SecurityUtils;
import com.seffafbagis.api.service.campaign.CampaignService;
import com.seffafbagis.api.service.receipt.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing payment sessions (shopping cart).
 * NOW: Stores cart items (campaignId + amount), creates donations at checkout.
 */
@Service
@RequiredArgsConstructor
public class PaymentSessionService {

    private final PaymentSessionRepository paymentSessionRepository;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final ReceiptService receiptService;
    private final CampaignService campaignService;

    /**
     * Get or create active cart for current user.
     */
    @Transactional
    public PaymentSession getOrCreateActiveSession(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return paymentSessionRepository
                .findByUserAndStatus(user, PaymentSessionStatus.PENDING)
                .orElseGet(() -> createNewSession(user));
    }

    /**
     * Get active cart session for current user.
     */
    @Transactional(readOnly = true)
    public PaymentSession getActiveSession() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BadRequestException("User not authenticated"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return paymentSessionRepository
                .findByUserAndStatus(user, PaymentSessionStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("No active cart session"));
    }

    /**
     * Add item to cart (NOT creating donation yet).
     */
    @Transactional
    public void addItemToCart(UUID userId, AddCartItemRequest request) {
        // Validate campaign exists
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));

        PaymentSession session = getOrCreateActiveSession(userId);

        if (session.getStatus() != PaymentSessionStatus.PENDING) {
            throw new BadRequestException("Cannot modify non-pending session");
        }

        // Add item to cart
        session.addCartItem(request.getCampaignId(), request.getAmount(), request.getCurrency());
        paymentSessionRepository.save(session);
    }

    /**
     * Remove item from cart.
     */
    @Transactional
    public void removeItemFromCart(UUID userId, UUID campaignId) {
        PaymentSession session = getOrCreateActiveSession(userId);
        session.removeCartItem(campaignId);
        paymentSessionRepository.save(session);
    }

    /**
     * Checkout: Create donations and receipts from cart items.
     */
    @Transactional
    public void checkout(UUID sessionId) {
        PaymentSession session = paymentSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment session not found"));

        if (session.getStatus() == PaymentSessionStatus.COMPLETED) {
            return; // Already completed
        }

        if (session.getCartItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        // Create donation for each cart item
        for (PaymentSession.CartItem item : session.getCartItems()) {
            Campaign campaign = campaignRepository.findById(item.getCampaignId())
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + item.getCampaignId()));

            Donation donation = new Donation();
            donation.setCampaign(campaign);
            donation.setDonor(session.getUser());
            donation.setAmount(item.getAmount());
            donation.setCurrency(item.getCurrency());
            donation.setPaymentSession(session);
            donation.setStatus(DonationStatus.COMPLETED); // Already paid (mock)
            donation.setIsAnonymous(false);

            donation = donationRepository.save(donation);

            // Generate receipt
            receiptService.createReceipt(donation);

            // Update campaign stats
            campaignService.incrementDonationStats(campaign.getId(), donation.getAmount());
        }

        // Mark session as completed and clear cart
        session.markAsCompleted();
        session.clearCart();
        paymentSessionRepository.save(session);
    }

    /**
     * Cleanup expired sessions.
     */
    @Transactional
    public void cleanupExpiredSessions() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusHours(24);
        List<PaymentSession> expiredSessions = paymentSessionRepository
                .findExpiredSessions(PaymentSessionStatus.PENDING, cutoff);

        for (PaymentSession session : expiredSessions) {
            session.setStatus(PaymentSessionStatus.EXPIRED);
            session.clearCart();
            paymentSessionRepository.save(session);
        }
    }

    private PaymentSession createNewSession(User user) {
        PaymentSession session = new PaymentSession(user, BigDecimal.ZERO);
        return paymentSessionRepository.save(session);
    }
}
