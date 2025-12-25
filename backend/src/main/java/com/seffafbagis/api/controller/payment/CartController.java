package com.seffafbagis.api.controller.payment;

import com.seffafbagis.api.dto.mapper.DonationMapper;
import com.seffafbagis.api.dto.request.payment.AddToCartRequest;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.donation.DonationResponse;
import com.seffafbagis.api.dto.response.payment.CartResponse;
import com.seffafbagis.api.entity.donation.PaymentSession;
import com.seffafbagis.api.security.SecurityUtils;
import com.seffafbagis.api.service.payment.PaymentSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Shopping cart controller.
 * Manages payment sessions for multiple donations.
 * 
 * Base URL: /api/v1/cart
 */
@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "Shopping cart operations")
@RequiredArgsConstructor
public class CartController {

    private final PaymentSessionService paymentSessionService;
    private final DonationMapper donationMapper;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current cart", description = "Get active payment session with all donations")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        PaymentSession session = paymentSessionService.getOrCreateActiveSession(userId);

        CartResponse response = new CartResponse(
                session.getId(),
                session.getTotalAmount(),
                session.getCurrency(),
                session.getDonations().stream()
                        .map(donationMapper::toResponse)
                        .collect(Collectors.toList()),
                session.getDonations().size(),
                session.getCreatedAt());

        return ResponseEntity.ok(ApiResponse.success("Cart retrieved", response));
    }

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add donation to cart", description = "Add an existing pending donation to cart")
    public ResponseEntity<ApiResponse<Void>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        PaymentSession session = paymentSessionService.getOrCreateActiveSession(userId);
        paymentSessionService.addDonationToSession(session.getId(), request.getDonationId());

        return ResponseEntity.ok(ApiResponse.success("Donation added to cart"));
    }

    @DeleteMapping("/remove/{donationId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove donation from cart")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@PathVariable UUID donationId) {
        PaymentSession session = paymentSessionService.getActiveSession();
        paymentSessionService.removeDonationFromSession(session.getId(), donationId);

        return ResponseEntity.ok(ApiResponse.success("Donation removed from cart"));
    }

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Checkout cart", description = "Complete payment session and generate receipts")
    public ResponseEntity<ApiResponse<Void>> checkout() {
        PaymentSession session = paymentSessionService.getActiveSession();

        // TODO: Integrate with PaymentService for actual payment
        // For now, just mark as completed
        paymentSessionService.completeSession(session.getId());

        return ResponseEntity.ok(ApiResponse.success("Cart checkout completed. Receipts generated."));
    }
}
