package com.seffafbagis.api.controller.payment;

import com.seffafbagis.api.dto.request.payment.AddCartItemRequest;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.payment.CartItemResponse;
import com.seffafbagis.api.entity.donation.PaymentSession;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.security.SecurityUtils;
import com.seffafbagis.api.service.payment.PaymentSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Shopping cart controller - REFACTORED to use items instead of donations.
 */
@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "Shopping cart operations")
@RequiredArgsConstructor
public class CartController {

    private final PaymentSessionService paymentSessionService;
    private final CampaignRepository campaignRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        try {
            PaymentSession session = paymentSessionService.getOrCreateActiveSession(userId);

            List<CartItemResponse> items = session.getCartItems().stream()
                    .map(item -> {
                        String campaignTitle = campaignRepository.findById(item.getCampaignId())
                                .map(c -> c.getTitle())
                                .orElse("Unknown Campaign");
                        return new CartItemResponse(
                                item.getCampaignId(),
                                campaignTitle,
                                item.getAmount(),
                                item.getCurrency());
                    })
                    .collect(Collectors.toList());

            CartResponse response = new CartResponse(
                    session.getId(),
                    session.getTotalAmount(),
                    session.getCurrency(),
                    items,
                    items.size());

            return ResponseEntity.ok(ApiResponse.success("Cart retrieved", response));
        } catch (Exception e) {
            // Return empty cart if error
            CartResponse emptyCart = new CartResponse(
                    null,
                    BigDecimal.ZERO,
                    "TRY",
                    List.of(),
                    0);
            return ResponseEntity.ok(ApiResponse.success("Empty cart", emptyCart));
        }
    }

    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add item to cart", description = "Add campaign + amount to cart (donation created at checkout)")
    public ResponseEntity<ApiResponse<Void>> addItem(@Valid @RequestBody AddCartItemRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        paymentSessionService.addItemToCart(userId, request);

        return ResponseEntity.ok(ApiResponse.success("Item added to cart"));
    }

    @DeleteMapping("/items/{campaignId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<Void>> removeItem(@PathVariable UUID campaignId) {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        paymentSessionService.removeItemFromCart(userId, campaignId);

        return ResponseEntity.ok(ApiResponse.success("Item removed from cart"));
    }

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Checkout", description = "Create donations and receipts from cart items")
    public ResponseEntity<ApiResponse<Void>> checkout() {
        PaymentSession session = paymentSessionService.getActiveSession();

        paymentSessionService.checkout(session.getId());

        return ResponseEntity.ok(ApiResponse.success("Checkout completed. Receipts generated."));
    }

    // Inner DTO class for response
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class CartResponse {
        private UUID sessionId;
        private BigDecimal totalAmount;
        private String currency;
        private List<CartItemResponse> items;
        private Integer itemCount;
    }
}
