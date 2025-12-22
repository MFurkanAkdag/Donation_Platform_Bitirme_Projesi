package com.seffafbagis.api.dto.request.donation;

import com.seffafbagis.api.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ProcessPaymentRequest {

    @NotNull(message = "Donation ID is required")
    private UUID donationId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
