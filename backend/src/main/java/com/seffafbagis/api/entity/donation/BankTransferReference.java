package com.seffafbagis.api.entity.donation;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.category.DonationType;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.organization.OrganizationBankAccount;
import com.seffafbagis.api.entity.user.User;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Represents a bank transfer reference code for matching Havale/EFT payments.
 * Donors use this code in transfer description to identify their donation.
 */
@Entity
@Table(name = "bank_transfer_references", indexes = {
        @Index(name = "idx_bank_ref_code", columnList = "reference_code", unique = true),
        @Index(name = "idx_bank_ref_status", columnList = "status"),
        @Index(name = "idx_bank_ref_expires", columnList = "expires_at")
})
public class BankTransferReference extends BaseEntity {

    @Column(name = "reference_code", nullable = false, unique = true, length = 20)
    private String referenceCode; // Format: SBP-YYYYMMDD-XXXXX

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign; // Nullable

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization; // Nullable

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private OrganizationBankAccount bankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id")
    private User donor; // Nullable for anonymous

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_type_id")
    private DonationType donationType; // Nullable

    @Column(name = "expected_amount", precision = 12, scale = 2)
    private BigDecimal expectedAmount;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "sender_iban", length = 34)
    private String senderIban;

    @Type(JsonType.class)
    @Column(name = "bank_account_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> bankAccountSnapshot;

    @Column(length = 20)
    private String status = "pending"; // 'pending', 'matched', 'expired'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_donation_id")
    private Donation matchedDonation; // Set when payment is matched

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    // Getters and Setters

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public OrganizationBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(OrganizationBankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public User getDonor() {
        return donor;
    }

    public void setDonor(User donor) {
        this.donor = donor;
    }

    public DonationType getDonationType() {
        return donationType;
    }

    public void setDonationType(DonationType donationType) {
        this.donationType = donationType;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public void setExpectedAmount(BigDecimal expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderIban() {
        return senderIban;
    }

    public void setSenderIban(String senderIban) {
        this.senderIban = senderIban;
    }

    public Map<String, Object> getBankAccountSnapshot() {
        return bankAccountSnapshot;
    }

    public void setBankAccountSnapshot(Map<String, Object> bankAccountSnapshot) {
        this.bankAccountSnapshot = bankAccountSnapshot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Donation getMatchedDonation() {
        return matchedDonation;
    }

    public void setMatchedDonation(Donation matchedDonation) {
        this.matchedDonation = matchedDonation;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
