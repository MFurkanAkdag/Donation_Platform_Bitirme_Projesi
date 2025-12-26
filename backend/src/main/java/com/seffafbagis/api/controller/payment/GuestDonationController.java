package com.seffafbagis.api.controller.payment;

import com.seffafbagis.api.dto.request.payment.GuestCheckoutRequest;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.payment.GuestCheckoutResponse;
import com.seffafbagis.api.service.payment.GuestDonationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for guest (anonymous) donations.
 * Allows users to donate without authentication.
 */
@RestController
@RequestMapping("/api/v1/guest")
@Tag(name = "Guest Donations", description = "Anonymous donation operations without authentication")
@RequiredArgsConstructor
@Slf4j
public class GuestDonationController {

    private final GuestDonationService guestDonationService;

    /**
     * Process guest checkout
     * Creates donations and receipts for anonymous users
     *
     * @param request Guest checkout request with cart items, guest info, and
     *                payment details
     * @return Response with created donations and receipts
     */
    @PostMapping("/checkout")
    @Operation(summary = "Guest checkout", description = "Process donation for guest users without authentication. " +
            "Creates donations, processes payment, and generates receipts. " +
            "Receipts are sent to the provided email address.")
    public ResponseEntity<ApiResponse<GuestCheckoutResponse>> guestCheckout(
            @Valid @RequestBody GuestCheckoutRequest request) {

        log.info("Guest checkout request received for email: {}", request.getGuestEmail());

        GuestCheckoutResponse response = guestDonationService.processGuestCheckout(request);

        return ResponseEntity.ok(ApiResponse.success(
                "Donation completed successfully!",
                response));
    }
}
