package com.seffafbagis.api.dto.request.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for guest (anonymous) checkout.
 * Guest users provide email/name and cart items, then checkout in a single API
 * call.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestCheckoutRequest {

    // Guest information
    @NotBlank(message = "Guest email is required")
    @Email(message = "Valid email address is required")
    private String guestEmail;

    @NotBlank(message = "Guest name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String guestName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String guestPhone;

    // Cart items
    @NotNull(message = "Cart items are required")
    @Size(min = 1, message = "At least one cart item is required")
    @Valid
    private List<CartItemRequest> cartItems;

    // Payment details
    @NotNull(message = "Payment details are required")
    @Valid
    private PaymentDetailsRequest paymentDetails;

    // Optional fields
    private String donorMessage;

    private Boolean isAnonymous = false;

    /**
     * Individual cart item for guest checkout
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemRequest {
        @NotNull(message = "Campaign ID is required")
        private UUID campaignId;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.0", message = "Amount must be at least 1")
        private BigDecimal amount;

        private String currency = "TRY";
    }

    /**
     * Payment details for guest checkout
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDetailsRequest {
        @NotBlank(message = "Card holder name is required")
        private String cardHolderName;

        @NotBlank(message = "Card number is required")
        @Size(min = 16, max = 16, message = "Card number must be 16 digits")
        @Pattern(regexp = "\\d+", message = "Card number must contain only digits")
        private String cardNumber;

        @NotBlank(message = "Expire month is required")
        @Size(min = 2, max = 2, message = "Expire month must be 2 digits")
        @Pattern(regexp = "(0[1-9]|1[0-2])", message = "Expire month must be between 01 and 12")
        private String expireMonth;

        @NotBlank(message = "Expire year is required")
        @Size(min = 4, max = 4, message = "Expire year must be 4 digits")
        @Pattern(regexp = "\\d{4}", message = "Expire year must be a valid year")
        private String expireYear;

        @NotBlank(message = "CVC is required")
        @Size(min = 3, max = 4, message = "CVC must be 3 or 4 digits")
        @Pattern(regexp = "\\d+", message = "CVC must contain only digits")
        private String cvc;

        private Boolean saveCard = false;
    }
}
