package com.seffafbagis.api.dto.response.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AdminOrganizationResponse {

    private UUID id;
    private String name;
    private String description;
    private String email;
    private String phone;
    private String website;
    private String verificationStatus; // PENDING, VERIFIED, REJECTED
    private String rejectionReason;
    private Integer resubmissionCount;
    private LocalDateTime verifiedAt;
    private AdminUserSummary verifiedBy;
    private List<DocumentInfo> documents;
    private List<BankAccountInfo> bankAccounts;
    private Integer campaignCount;
    private BigDecimal totalRaised;
    private LocalDateTime createdAt;

    public AdminOrganizationResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
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

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public AdminUserSummary getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(AdminUserSummary verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public List<DocumentInfo> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentInfo> documents) {
        this.documents = documents;
    }

    public List<BankAccountInfo> getBankAccounts() {
        return bankAccounts;
    }

    public void setBankAccounts(List<BankAccountInfo> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

    public Integer getCampaignCount() {
        return campaignCount;
    }

    public void setCampaignCount(Integer campaignCount) {
        this.campaignCount = campaignCount;
    }

    public BigDecimal getTotalRaised() {
        return totalRaised;
    }

    public void setTotalRaised(BigDecimal totalRaised) {
        this.totalRaised = totalRaised;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static class AdminUserSummary {
        private UUID id;
        private String email;
        private String fullName;

        public AdminUserSummary() {
        }

        public AdminUserSummary(UUID id, String email, String fullName) {
            this.id = id;
            this.email = email;
            this.fullName = fullName;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }

    public static class DocumentInfo {
        private UUID id;
        private String documentType;
        private String fileName;
        private LocalDateTime uploadedAt;

        public DocumentInfo() {
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getDocumentType() {
            return documentType;
        }

        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public LocalDateTime getUploadedAt() {
            return uploadedAt;
        }

        public void setUploadedAt(LocalDateTime uploadedAt) {
            this.uploadedAt = uploadedAt;
        }
    }

    public static class BankAccountInfo {
        private UUID id;
        private String bankName;
        private String ibanMasked;
        private Boolean isVerified;

        public BankAccountInfo() {
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public String getIbanMasked() {
            return ibanMasked;
        }

        public void setIbanMasked(String ibanMasked) {
            this.ibanMasked = ibanMasked;
        }

        public Boolean getIsVerified() {
            return isVerified;
        }

        public void setIsVerified(Boolean isVerified) {
            this.isVerified = isVerified;
        }
    }
}
