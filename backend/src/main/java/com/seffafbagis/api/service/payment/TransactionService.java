package com.seffafbagis.api.service.payment;

import com.iyzipay.model.PaymentResource;
import com.seffafbagis.api.dto.request.payment.PaymentRequest;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.donation.Transaction;
import com.seffafbagis.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction createTransaction(Donation donation, PaymentRequest request, PaymentResource response,
            boolean is3dSecure) {
        Transaction transaction = new Transaction();
        transaction.setDonation(donation);
        transaction.setPaymentMethod(com.seffafbagis.api.enums.PaymentMethod.CREDIT_CARD);
        transaction.setPaymentProvider("IYZICO");
        transaction.setProviderTransactionId(response.getPaymentId());
        transaction.setProviderPaymentId(response.getConversationId());

        transaction.setAmount(donation.getAmount());

        // Note: For now we don't calculate fees strictly from response as it might vary
        // Typically we would get paidPrice from response
        if (response.getPaidPrice() != null) {
            transaction.setNetAmount(response.getPaidPrice());
            // Fee calculation logic could be complex, simplifying for now
            transaction.setFeeAmount(BigDecimal.ZERO);
        } else {
            transaction.setNetAmount(donation.getAmount());
            transaction.setFeeAmount(BigDecimal.ZERO);
        }

        if (request != null) {
            transaction.setCardLastFour(request.getCardNumber().substring(request.getCardNumber().length() - 4));
            // transaction.setCardBrand("UNKNOWN"); // Could parse from bin number
        }

        transaction.setIs3dSecure(is3dSecure);
        transaction.setStatus(response.getStatus()); // success or failure

        // Store raw response if possible (JsonB)
        // transaction.setRawResponse(response.toString());

        if (!"success".equals(response.getStatus())) {
            transaction.setErrorCode(response.getErrorCode());
            transaction.setErrorMessage(response.getErrorMessage());
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction updateTransactionStatus(UUID transactionId, String status) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        transaction.setStatus(status);
        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public Transaction getTransaction(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    @Transactional(readOnly = true)
    public Transaction getTransactionByDonation(UUID donationId) {
        return transactionRepository.findByDonationId(donationId)
                .orElseThrow(() -> new RuntimeException("Transaction not found for donation"));
    }

    @Transactional
    public Transaction recordRefund(UUID transactionId, BigDecimal amount) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        transaction.setRefundedAmount(amount);
        transaction.setRefundedAt(java.time.OffsetDateTime.now());
        transaction.setStatus("REFUNDED");
        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Transaction> getTransactionsByStatus(String status,
            org.springframework.data.domain.Pageable pageable) {
        return transactionRepository.findByStatus(status, pageable);
    }
}
