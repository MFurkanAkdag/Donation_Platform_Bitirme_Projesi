package com.seffafbagis.api.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

public class PaymentResultResponse {
    private boolean success;
    private UUID donationId;
    private UUID transactionId;
    private String status;
    private String message;
    private String receiptNumber;

    public PaymentResultResponse() {
    }

    public PaymentResultResponse(boolean success, UUID donationId, UUID transactionId, String status, String message,
            String receiptNumber) {
        this.success = success;
        this.donationId = donationId;
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
        this.receiptNumber = receiptNumber;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public UUID getDonationId() {
        return donationId;
    }

    public void setDonationId(UUID donationId) {
        this.donationId = donationId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public static PaymentResultResponseBuilder builder() {
        return new PaymentResultResponseBuilder();
    }

    public static class PaymentResultResponseBuilder {
        private boolean success;
        private UUID donationId;
        private UUID transactionId;
        private String status;
        private String message;
        private String receiptNumber;

        public PaymentResultResponseBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public PaymentResultResponseBuilder donationId(UUID donationId) {
            this.donationId = donationId;
            return this;
        }

        public PaymentResultResponseBuilder transactionId(UUID transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public PaymentResultResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public PaymentResultResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public PaymentResultResponseBuilder receiptNumber(String receiptNumber) {
            this.receiptNumber = receiptNumber;
            return this;
        }

        public PaymentResultResponse build() {
            return new PaymentResultResponse(success, donationId, transactionId, status, message, receiptNumber);
        }
    }
}
