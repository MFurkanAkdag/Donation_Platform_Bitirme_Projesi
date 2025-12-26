package com.seffafbagis.api.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for guest checkout.
 * Returns created donations and receipts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestCheckoutResponse {

    private boolean success;
    private String message;

    private List<DonationReceiptInfo> donations;

    private BigDecimal totalAmount;
    private String currency;
    private String guestEmail;

    /**
     * Information about created donation and receipt
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DonationReceiptInfo {
        private UUID donationId;
        private UUID receiptId;
        private UUID campaignId;
        private String campaignTitle;
        private BigDecimal amount;
        private String currency;
        private String receiptNumber; // barcode data
        private String receiptPdfUrl;
    }
}
