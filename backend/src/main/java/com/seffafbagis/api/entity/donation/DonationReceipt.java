package com.seffafbagis.api.entity.donation;

import com.seffafbagis.api.entity.base.BaseEntity;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Represents a digital receipt for a donation.
 * Generated after successful payment completion.
 */
@Entity
@Table(name = "donation_receipts", indexes = {
        @Index(name = "idx_receipts_receipt_number", columnList = "receipt_number", unique = true)
})
public class DonationReceipt extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_id", nullable = false, unique = true)
    private Donation donation;

    @Column(name = "receipt_number", nullable = false, unique = true, length = 50)
    private String receiptNumber; // Format: RCPT-YYYY-NNNNNN

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Column(name = "issued_at")
    private OffsetDateTime issuedAt;

    // Getters and Setters

    public Donation getDonation() {
        return donation;
    }

    public void setDonation(Donation donation) {
        this.donation = donation;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }

    public OffsetDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(OffsetDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}
