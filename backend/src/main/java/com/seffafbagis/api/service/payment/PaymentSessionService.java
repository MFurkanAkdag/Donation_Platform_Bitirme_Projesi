package com.seffafbagis.api.service.payment;

import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.donation.PaymentSession;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.DonationStatus;
import com.seffafbagis.api.enums.PaymentSessionStatus;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.DonationRepository;
import com.seffafbagis.api.repository.PaymentSessionRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.SecurityUtils;
import com.seffafbagis.api.service.donation.DonationService;
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
 * Handles cart operations: add, remove, checkout.
 * 
 * @author System
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class PaymentSessionService {

    private final PaymentSessionRepository paymentSessionRepository;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final DonationService donationService;
    private final ReceiptService receiptService;

    /**
     * Get or create active cart for current user.
     */
    @Transactional
    public PaymentSession getOrCreateActiveSession(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PaymentSession session = paymentSessionRepository
                .findByUserAndStatus(user, PaymentSessionStatus.PENDING)
                .orElseGet(() -> createNewSession(user));

        // Force load donations to avoid lazy initialization exception
        session.getDonations().size();

        return session;
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

        PaymentSession session = paymentSessionRepository
                .findByUserAndStatus(user, PaymentSessionStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("No active cart session"));

        // Force load donations to avoid lazy initialization exception
        session.getDonations().size();

        return session;
    }

    /**
     * Add a donation to cart (link it to payment session).
     */
    @Transactional
    public void addDonationToSession(UUID sessionId, UUID donationId) {
        PaymentSession session = paymentSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment session not found"));

        if (session.getStatus() != PaymentSessionStatus.PENDING) {
            throw new BadRequestException("Cannot modify non-pending session");
        }

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));

        if (donation.getStatus() != DonationStatus.PENDING) {
            throw new BadRequestException("Can only add pending donations to cart");
        }

        // Link donation to session
        donation.setPaymentSession(session);
        donationRepository.save(donation);

        // Recalculate total
        session.recalculateTotalAmount();
        paymentSessionRepository.save(session);
    }

    /**
     * Remove a donation from cart.
     */
    @Transactional
    public void removeDonationFromSession(UUID sessionId, UUID donationId) {
        PaymentSession session = paymentSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment session not found"));

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));

        if (donation.getPaymentSession() == null ||
                !donation.getPaymentSession().getId().equals(sessionId)) {
            throw new BadRequestException("Donation not in this session");
        }

        // Unlink donation
        donation.setPaymentSession(null);
        donationRepository.save(donation);

        // Recalculate total
        session.recalculateTotalAmount();
        paymentSessionRepository.save(session);
    }

    /**
     * Mark session as completed and generate receipts for all donations.
     * Called after successful payment.
     */
    @Transactional
    public void completeSession(UUID sessionId) {
        PaymentSession session = paymentSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment session not found"));

        if (session.getStatus() == PaymentSessionStatus.COMPLETED) {
            return; // Already completed
        }

        // Mark session as complete
        session.markAsCompleted();
        paymentSessionRepository.save(session);

        // Complete all donations and generate receipts
        List<Donation> donations = session.getDonations();
        for (Donation donation : donations) {
            donationService.completeDonation(donation.getId());
            // completeDonation already calls receiptService.createReceipt()
        }
    }

    /**
     * Get user's payment history (completed sessions).
     */
    @Transactional(readOnly = true)
    public List<PaymentSession> getUserPaymentHistory(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return paymentSessionRepository.findByUserAndStatusOrderByCompletedAtDesc(
                user,
                PaymentSessionStatus.COMPLETED);
    }

    /**
     * Cleanup expired sessions (older than 24 hours and still pending).
     * Should be called by scheduled job.
     */
    @Transactional
    public void cleanupExpiredSessions() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusHours(24);
        List<PaymentSession> expiredSessions = paymentSessionRepository
                .findExpiredSessions(PaymentSessionStatus.PENDING, cutoff);

        for (PaymentSession session : expiredSessions) {
            session.setStatus(PaymentSessionStatus.EXPIRED);

            // Unlink donations
            for (Donation donation : session.getDonations()) {
                donation.setPaymentSession(null);
            }

            paymentSessionRepository.save(session);
        }
    }

    private PaymentSession createNewSession(User user) {
        PaymentSession session = new PaymentSession(user, BigDecimal.ZERO);
        return paymentSessionRepository.save(session);
    }
}
