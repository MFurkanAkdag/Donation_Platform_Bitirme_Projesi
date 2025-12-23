package com.seffafbagis.api.entity.organization;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.OrganizationType;
import com.seffafbagis.api.enums.VerificationStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organizations", indexes = {
        @Index(name = "idx_organizations_user_id", columnList = "user_id"),
        @Index(name = "idx_organizations_verification", columnList = "verification_status"),
        @Index(name = "idx_organizations_type", columnList = "organization_type")
})
public class Organization extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "organization_type", nullable = false)
    private OrganizationType organizationType;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "trade_name")
    private String tradeName;

    @Column(name = "tax_number", unique = true)
    private String taxNumber;

    @Column(name = "derbis_number")
    private String derbisNumber;

    @Column(name = "mersis_number")
    private String mersisNumber;

    @Column(name = "establishment_date")
    private LocalDate establishmentDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "mission_statement", columnDefinition = "TEXT")
    private String missionStatement;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "website_url")
    private String websiteUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "resubmission_count")
    private Integer resubmissionCount = 0;

    @Column(name = "last_resubmission_at")
    private LocalDateTime lastResubmissionAt;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    // Relationships
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrganizationContact> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrganizationDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrganizationBankAccount> bankAccounts = new ArrayList<>();

    // Getters and Setters

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(OrganizationType organizationType) {
        this.organizationType = organizationType;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public String getDerbisNumber() {
        return derbisNumber;
    }

    public void setDerbisNumber(String derbisNumber) {
        this.derbisNumber = derbisNumber;
    }

    public String getMersisNumber() {
        return mersisNumber;
    }

    public void setMersisNumber(String mersisNumber) {
        this.mersisNumber = mersisNumber;
    }

    public LocalDate getEstablishmentDate() {
        return establishmentDate;
    }

    public void setEstablishmentDate(LocalDate establishmentDate) {
        this.establishmentDate = establishmentDate;
    }

    public String DESCRIPTION() {
        return description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMissionStatement() {
        return missionStatement;
    }

    public void setMissionStatement(String missionStatement) {
        this.missionStatement = missionStatement;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public User getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(User verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Integer getResubmissionCount() {
        return resubmissionCount;
    }

    public void setResubmissionCount(Integer resubmissionCount) {
        this.resubmissionCount = resubmissionCount;
    }

    public LocalDateTime getLastResubmissionAt() {
        return lastResubmissionAt;
    }

    public void setLastResubmissionAt(LocalDateTime lastResubmissionAt) {
        this.lastResubmissionAt = lastResubmissionAt;
    }

    public Boolean getFeatured() {
        return isFeatured;
    }

    public void setFeatured(Boolean featured) {
        isFeatured = featured;
    }

    public List<OrganizationContact> getContacts() {
        return contacts;
    }

    public void setContacts(List<OrganizationContact> contacts) {
        this.contacts = contacts;
    }

    public List<OrganizationDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<OrganizationDocument> documents) {
        this.documents = documents;
    }

    public List<OrganizationBankAccount> getBankAccounts() {
        return bankAccounts;
    }

    public void setBankAccounts(List<OrganizationBankAccount> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

    // Helper methods for relationships
    public void addContact(OrganizationContact contact) {
        contacts.add(contact);
        contact.setOrganization(this);
    }

    public void removeContact(OrganizationContact contact) {
        contacts.remove(contact);
        contact.setOrganization(null);
    }

    public void addDocument(OrganizationDocument document) {
        documents.add(document);
        document.setOrganization(this);
    }

    public void removeDocument(OrganizationDocument document) {
        documents.remove(document);
        document.setOrganization(null);
    }

    public void addBankAccount(OrganizationBankAccount bankAccount) {
        bankAccounts.add(bankAccount);
        bankAccount.setOrganization(this);
    }

    public void removeBankAccount(OrganizationBankAccount bankAccount) {
        bankAccounts.remove(bankAccount);
        bankAccount.setOrganization(null);
    }
}
