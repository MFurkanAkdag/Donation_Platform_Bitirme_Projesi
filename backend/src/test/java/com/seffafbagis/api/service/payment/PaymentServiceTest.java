package com.seffafbagis.api.service.payment;

import com.iyzipay.model.ThreedsInitialize;
import com.iyzipay.model.ThreedsPayment;
import com.seffafbagis.api.dto.request.payment.PaymentRequest;
import com.seffafbagis.api.dto.request.payment.ThreeDSCallbackRequest;
import com.seffafbagis.api.dto.response.payment.PaymentResultResponse;
import com.seffafbagis.api.dto.response.payment.ThreeDSInitResponse;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.donation.Transaction;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.DonationStatus;
import com.seffafbagis.api.exception.PaymentException;
import com.seffafbagis.api.repository.DonationRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.CustomUserDetails;
import com.seffafbagis.api.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private IyzicoService iyzicoService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentService paymentService;

    private User user;
    private Donation donation;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");

        donation = new Donation();
        donation.setId(UUID.randomUUID());
        donation.setAmount(new BigDecimal("100.00"));
        donation.setStatus(DonationStatus.PENDING);

        paymentRequest = new PaymentRequest();
        paymentRequest.setDonationId(donation.getId());
        paymentRequest.setCardHolderName("Test User");
        paymentRequest.setCardNumber("1111111111111111");
        paymentRequest.setExpireMonth("12");
        paymentRequest.setExpireYear("2030");
        paymentRequest.setCvc("123");
    }

    @Test
    void initializePayment_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(user.getId()));

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(donationRepository.findById(donation.getId())).thenReturn(Optional.of(donation));

            ThreedsInitialize iyzicoResponse = new ThreedsInitialize();
            iyzicoResponse.setHtmlContent("<html>...</html>");
            iyzicoResponse.setPaymentId("payment123");
            iyzicoResponse.setStatus("success");

            when(iyzicoService.create3DSPayment(any(PaymentRequest.class), any(Donation.class), any(User.class)))
                    .thenReturn(iyzicoResponse);

            ThreeDSInitResponse response = paymentService.initializePayment(paymentRequest);

            assertNotNull(response);
            assertEquals("<html>...</html>", response.getThreeDSHtmlContent());
            assertEquals("payment123", response.getPaymentId());
        }
    }

    @Test
    void handle3DSCallback_Success() {
        ThreeDSCallbackRequest callbackRequest = new ThreeDSCallbackRequest();
        callbackRequest.setStatus("success");
        callbackRequest.setPaymentId("payment123");
        callbackRequest.setConversationId(donation.getId().toString());

        ThreedsPayment threedsPayment = new ThreedsPayment();
        threedsPayment.setStatus("success");
        threedsPayment.setPrice(new BigDecimal("100.00"));

        when(iyzicoService.complete3DSPayment("payment123", donation.getId().toString()))
                .thenReturn(threedsPayment);

        when(donationRepository.findById(donation.getId())).thenReturn(Optional.of(donation));

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());

        when(transactionService.createTransaction(any(Donation.class), isNull(), any(ThreedsPayment.class), eq(true)))
                .thenReturn(transaction);

        PaymentResultResponse response = paymentService.handle3DSCallback(callbackRequest);

        assertTrue(response.isSuccess());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(DonationStatus.COMPLETED, donation.getStatus());
        verify(donationRepository).save(donation);
    }

    @Test
    void refundPayment_Success() {
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setStatus("success");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setProviderTransactionId("provider_tx_123");

        when(transactionService.getTransaction(transactionId)).thenReturn(transaction);

        com.iyzipay.model.Refund refundResponse = new com.iyzipay.model.Refund();
        refundResponse.setStatus("success");

        when(iyzicoService.createRefund(any(Transaction.class), any(BigDecimal.class)))
                .thenReturn(refundResponse);

        when(transactionService.updateTransactionStatus(transactionId, "REFUNDED")).thenReturn(transaction);

        com.seffafbagis.api.dto.request.payment.RefundPaymentRequest request = com.seffafbagis.api.dto.request.payment.RefundPaymentRequest
                .builder()
                .transactionId(transactionId)
                .amount(new BigDecimal("100.00"))
                .reason("Customer request")
                .build();

        com.seffafbagis.api.dto.response.payment.RefundResultResponse response = paymentService.refundPayment(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(transactionId, response.getTransactionId());
        assertEquals(new BigDecimal("100.00"), response.getRefundedAmount());
        verify(transactionService).updateTransactionStatus(transactionId, "REFUNDED");
    }

    @Test
    void refundPayment_Failure_InvalidTransactionStatus() {
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setStatus("failed"); // Not a successful transaction

        when(transactionService.getTransaction(transactionId)).thenReturn(transaction);

        com.seffafbagis.api.dto.request.payment.RefundPaymentRequest request = com.seffafbagis.api.dto.request.payment.RefundPaymentRequest
                .builder()
                .transactionId(transactionId)
                .amount(new BigDecimal("100.00"))
                .reason("Customer request")
                .build();

        assertThrows(PaymentException.class, () -> paymentService.refundPayment(request));
        verify(iyzicoService, never()).createRefund(any(), any());
    }
}
