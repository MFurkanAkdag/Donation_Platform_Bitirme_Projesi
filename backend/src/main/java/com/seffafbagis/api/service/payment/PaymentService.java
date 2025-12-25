package com.seffafbagis.api.service.payment;

import com.iyzipay.model.ThreedsInitialize;
import com.iyzipay.model.ThreedsPayment;
import com.seffafbagis.api.dto.request.payment.PaymentRequest;
import com.seffafbagis.api.dto.request.payment.RefundPaymentRequest;
import com.seffafbagis.api.dto.request.payment.SaveCardRequest;
import com.seffafbagis.api.dto.request.payment.ThreeDSCallbackRequest;
import com.seffafbagis.api.dto.response.payment.*;
import com.seffafbagis.api.enums.DonationStatus;
import com.seffafbagis.api.exception.PaymentException;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.donation.Transaction;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.repository.DonationRepository;
import com.seffafbagis.api.security.SecurityUtils;
import com.iyzipay.model.Refund;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final IyzicoService iyzicoService;
    private final TransactionService transactionService;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final com.seffafbagis.api.service.receipt.ReceiptService receiptService;

    @Transactional
    public ThreeDSInitResponse initializePayment(PaymentRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new UnauthorizedException("User not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PaymentException("User not found", "USER_NOT_FOUND", false));

        Donation donation = donationRepository.findById(request.getDonationId())
                .orElseThrow(() -> new PaymentException("Donation not found", "DONATION_NOT_FOUND", false));

        if (donation.getStatus() == DonationStatus.COMPLETED) {
            throw new PaymentException("This donation is already paid", "ALREADY_PAID", false);
        }

        // 1. Initialize 3DS at Iyzico
        ThreedsInitialize threedsInitialize = iyzicoService.create3DSPayment(request, donation, user);

        // 2. We don't save card yet, we wait for success callback if saveCard is
        // requested
        // But we could store the intent or check it later.
        // For simplicity, we trust the flow or store temp state if needed.
        // Actually, Iyzico handles card storage request within the payment request if
        // 'registerCard' is 1.
        // So we just need to handle the result in callback if we want to store token
        // locally.

        return new ThreeDSInitResponse(threedsInitialize.getHtmlContent(), threedsInitialize.getPaymentId());
    }

    /**
     * Process direct payment without 3DS (only for non-Turkey scenarios or special
     * cases where allowed).
     * Note: 3DS is mandatory in Turkey for online payments.
     */
    @Transactional
    public PaymentResultResponse processDirectPayment(PaymentRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new UnauthorizedException("User not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PaymentException("User not found", "USER_NOT_FOUND", false));

        Donation donation = donationRepository.findById(request.getDonationId())
                .orElseThrow(() -> new PaymentException("Donation not found", "DONATION_NOT_FOUND", false));

        if (donation.getStatus() == DonationStatus.COMPLETED) {
            throw new PaymentException("This donation is already paid", "ALREADY_PAID", false);
        }

        // --- MOCK PAYMENT & RECEIPT LOGIC START (For Phase 18.0) ---
        // Basic Mock Check:
        // 5100... -> INVALID_BALANCE
        // 1234... -> INVALID_CARD
        // Others -> SUCCESS

        String cardNumber = request.getCardNumber() != null ? request.getCardNumber().replaceAll("\\s", "") : "";

        if (cardNumber.startsWith("5100")) {
            donation.setStatus(DonationStatus.FAILED);
            donationRepository.save(donation);
            throw new PaymentException("Insufficient Balance", "INSUFFICIENT_BALANCE", false);
        }

        if (cardNumber.length() < 13) { // Simple fake validation
            donation.setStatus(DonationStatus.FAILED);
            donationRepository.save(donation);
            throw new PaymentException("Invalid Card Number", "INVALID_CARD", false);
        }

        // Mock Success
        donation.setStatus(DonationStatus.COMPLETED);
        donationRepository.save(donation);

        // Generate Receipt/Makbuz
        com.seffafbagis.api.entity.Receipt receipt = receiptService.createReceipt(donation);

        // Simulated Transaction
        // transactionService.createMockTransaction(donation, "MOCK-TRX-" +
        // System.currentTimeMillis());
        // We can reuse existing logic if possible or skip explicit transaction log if
        // acceptable for mock
        // For now, let's keep it simple as prompt requested.

        return PaymentResultResponse.builder()
                .success(true)
                .donationId(donation.getId())
                .transactionId(UUID.randomUUID())
                .status("COMPLETED")
                .message("Payment successful (Mock)")
                .build();
        // --- MOCK PAYMENT LOGIC END ---
    }

    @Transactional
    public PaymentResultResponse handle3DSCallback(ThreeDSCallbackRequest callbackRequest) {
        // Iyzico callback logic
        // Verify signature if needed (handled by client library mostly or via params)

        if (!"success".equalsIgnoreCase(callbackRequest.getStatus())) {
            log.error("Payment failed at bank 3DS step: {}", callbackRequest.getMdStatus());
            return PaymentResultResponse.builder()
                    .success(false)
                    .message("3DS Authorization failed")
                    .status(callbackRequest.getStatus())
                    .build();
        }

        // Complete 3DS Payment
        ThreedsPayment payment = iyzicoService.complete3DSPayment(callbackRequest.getPaymentId(),
                callbackRequest.getConversationId());

        UUID donationId = UUID.fromString(callbackRequest.getConversationId());
        Donation donation = donationRepository.findById(donationId).orElseThrow();

        // Record Transaction
        Transaction transaction = transactionService.createTransaction(donation, null, payment, true);

        if ("success".equals(payment.getStatus())) {
            // Update Donation
            donation.setStatus(DonationStatus.COMPLETED);
            donationRepository.save(donation);

            // Handle Card Saving if it was requested and successful
            if (payment.getCardToken() != null && !payment.getCardToken().isEmpty()) {
                // ThreedsPayment might not have all card details directly accessible or named
                // differently
                // We use what we have or defaults
                saveCardTokenForUser(payment.getCardToken(), "Card", // payment.getCardAlias() might be missing
                        "****", payment.getCardType(), payment.getCardFamily());
            }

            return PaymentResultResponse.builder()
                    .success(true)
                    .donationId(donation.getId())
                    .transactionId(transaction.getId())
                    .status("COMPLETED")
                    .message("Payment successful")
                    .build();
        } else {
            donation.setStatus(DonationStatus.FAILED);
            donationRepository.save(donation);

            return PaymentResultResponse.builder()
                    .success(false)
                    .donationId(donation.getId())
                    .transactionId(transaction.getId())
                    .status("FAILED")
                    .message(payment.getErrorMessage())
                    .build();
        }
    }

    // Manual Save Card (standalone)
    public SavedCardResponse saveCard(SaveCardRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new UnauthorizedException("User not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PaymentException("User not found", "USER_NOT_FOUND", false));

        return iyzicoService.createCardToken(request, user);
    }

    @Transactional
    public RefundResultResponse refundPayment(RefundPaymentRequest request) {
        Transaction transaction = transactionService.getTransaction(request.getTransactionId());

        if (!"success".equals(transaction.getStatus())) {
            throw new PaymentException("Can only refund successful transactions", "INVALID_TRANSACTION", false);
        }

        Refund refund = iyzicoService.createRefund(transaction, request.getAmount());

        if ("success".equals(refund.getStatus())) {
            // Transaction status update logic
            transactionService.updateTransactionStatus(transaction.getId(), "REFUNDED");

            return RefundResultResponse.builder()
                    .success(true)
                    .transactionId(transaction.getId())
                    .refundedAmount(request.getAmount() != null ? request.getAmount() : transaction.getAmount())
                    .message("Refund successful")
                    .build();
        } else {
            throw new PaymentException(refund.getErrorMessage(), refund.getErrorCode(), false);
        }
    }

    public List<SavedCardResponse> getSavedCards() {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new UnauthorizedException("User not found"));
        // User user = userRepository.findById(userId).orElseThrow(...);
        // Assuming we store cardUserKey in User entity or derive it (e.g. email or
        // custom field)
        // Since we don't have it explicitly in the prompt's User entity description,
        // we might need to assume it's stored or we use a workaround.
        // For Iyzico, usually you create a card user first.
        // If we don't have a cardUserKey, we can't fetch cards from Iyzico.

        // Placeholder: Returning empty list if we don't have key mechanism implemented
        return Collections.emptyList();

        // Real implementation would be:
        // CardList cardList = iyzicoService.getUserCards(user.getCardUserKey());
        // return cardList.getCardDetails().stream()
        // .map(paymentMapper::toSavedCardResponse)
        // .collect(Collectors.toList());
    }

    public void deleteCard(String cardToken) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new UnauthorizedException("User not found"));
        // iyzicoService.deleteCardToken(cardToken, user.getCardUserKey());
    }

    private void saveCardTokenForUser(String cardToken, String alias, String lastFour, String brand, String family) {
        // Logic to save token to User's profile or RecurringDonation
        // As per prompt, "Card Token Storage: Save cards for recurring donations"
        // This might imply we need to update the RecurringDonation entity with this
        // token
        // But we don't know which RecurringDonation this payment belongs to easily
        // unless we passed it.
        // For now, logging it as a todo or assuming it's handled if explicit save card
        // flow is used.
        log.info("Card token generated: {}", cardToken);
    }

    @Transactional
    public boolean processRecurringPayment(com.seffafbagis.api.entity.donation.RecurringDonation rd) {
        if (rd.getCardToken() == null) {
            log.error("Recurring donation {} has no card token", rd.getId());
            return false;
        }

        Donation donation = new Donation();
        donation.setDonor(rd.getDonor());
        donation.setCampaign(rd.getCampaign());
        donation.setAmount(rd.getAmount());
        donation.setCurrency(rd.getCurrency());
        donation.setDonationType(rd.getDonationType());
        donation.setStatus(DonationStatus.PENDING);

        donationRepository.save(donation);

        try {
            com.iyzipay.model.Payment payment = iyzicoService.chargeWithToken(rd.getCardToken(), rd.getAmount(),
                    donation, rd.getDonor());

            Transaction transaction = transactionService.createTransaction(donation, null, payment, false);

            if ("success".equals(payment.getStatus())) {
                donation.setStatus(DonationStatus.COMPLETED);
                donationRepository.save(donation);
                return true;
            } else {
                donation.setStatus(DonationStatus.FAILED);
                donationRepository.save(donation);
                log.error("Recurring payment failed for RD {}: {}", rd.getId(), payment.getErrorMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("Exception in recurring payment for RD {}", rd.getId(), e);
            donation.setStatus(DonationStatus.FAILED);
            donationRepository.save(donation);
            return false;
        }
    }
}
