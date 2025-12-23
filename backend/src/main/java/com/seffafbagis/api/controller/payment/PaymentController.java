package com.seffafbagis.api.controller.payment;

import com.seffafbagis.api.dto.request.payment.PaymentRequest;
import com.seffafbagis.api.dto.request.payment.RefundPaymentRequest;
import com.seffafbagis.api.dto.request.payment.SaveCardRequest;
import com.seffafbagis.api.dto.request.payment.ThreeDSCallbackRequest;
import com.seffafbagis.api.dto.response.payment.PaymentResultResponse;
import com.seffafbagis.api.dto.response.payment.RefundResultResponse;
import com.seffafbagis.api.dto.response.payment.SavedCardResponse;
import com.seffafbagis.api.dto.response.payment.ThreeDSInitResponse;
import com.seffafbagis.api.entity.donation.Transaction;
import com.seffafbagis.api.service.payment.PaymentService;
import com.seffafbagis.api.service.payment.TransactionService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final TransactionService transactionService;

    @org.springframework.beans.factory.annotation.Value("${app.frontend-url}")
    private String frontendUrl;

    @PostMapping("/initialize")
    public ResponseEntity<ThreeDSInitResponse> initializePayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.initializePayment(request));
    }

    @PostMapping(value = "/callback/3ds", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void handle3DSCallback(
            @RequestParam("status") String status,
            @RequestParam("paymentId") String paymentId,
            @RequestParam("conversationId") String conversationId,
            @RequestParam(value = "mdStatus", required = false) String mdStatus,
            HttpServletResponse response) throws IOException {

        ThreeDSCallbackRequest callbackRequest = ThreeDSCallbackRequest.builder()
                .status(status)
                .paymentId(paymentId)
                .conversationId(conversationId)
                .mdStatus(mdStatus)
                .build();

        PaymentResultResponse result = paymentService.handle3DSCallback(callbackRequest);

        // Redirect to frontend result page
        String redirectUrl = result.isSuccess()
                ? frontendUrl + "/payment/success?donationId=" + result.getDonationId()
                : frontendUrl + "/payment/failure?donationId=" + result.getDonationId() + "&message="
                        + result.getMessage();

        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable UUID transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }

    @PostMapping("/{transactionId}/refund")
    public ResponseEntity<RefundResultResponse> refundPayment(
            @PathVariable UUID transactionId,
            @Valid @RequestBody RefundPaymentRequest request) {
        // Ensure path ID matches body ID or set it
        request.setTransactionId(transactionId);
        return ResponseEntity.ok(paymentService.refundPayment(request));
    }

    @PostMapping("/cards")
    public ResponseEntity<SavedCardResponse> saveCard(@Valid @RequestBody SaveCardRequest request) {
        return ResponseEntity.ok(paymentService.saveCard(request));
    }

    @GetMapping("/cards")
    public ResponseEntity<List<SavedCardResponse>> getSavedCards() {
        return ResponseEntity.ok(paymentService.getSavedCards());
    }

    @DeleteMapping("/cards/{token}")
    public ResponseEntity<Void> deleteCard(@PathVariable String token) {
        paymentService.deleteCard(token);
        return ResponseEntity.noContent().build();
    }
}
