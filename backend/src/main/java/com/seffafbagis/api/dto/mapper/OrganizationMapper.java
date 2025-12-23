package com.seffafbagis.api.dto.mapper;

import com.seffafbagis.api.dto.request.organization.*;
import com.seffafbagis.api.dto.response.organization.*;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.organization.OrganizationBankAccount;
import com.seffafbagis.api.entity.organization.OrganizationContact;
import com.seffafbagis.api.entity.organization.OrganizationDocument;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.VerificationStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrganizationMapper {

    // Organization mappings
    public Organization toEntity(CreateOrganizationRequest request, User user) {
        Organization organization = new Organization();
        organization.setUser(user);
        organization.setOrganizationType(request.getOrganizationType());
        organization.setLegalName(request.getLegalName());
        organization.setTradeName(request.getTradeName());
        organization.setTaxNumber(request.getTaxNumber());
        organization.setDerbisNumber(request.getDerbisNumber());
        organization.setMersisNumber(request.getMersisNumber());
        organization.setEstablishmentDate(request.getEstablishmentDate());
        organization.setDescription(request.getDescription());
        organization.setMissionStatement(request.getMissionStatement());
        organization.setLogoUrl(request.getLogoUrl());
        organization.setWebsiteUrl(request.getWebsiteUrl());

        // Defaults
        organization.setVerificationStatus(VerificationStatus.PENDING);
        organization.setFeatured(false);
        organization.setResubmissionCount(0);

        return organization;
    }

    public void updateEntity(Organization entity, UpdateOrganizationRequest request) {
        if (request.getOrganizationType() != null)
            entity.setOrganizationType(request.getOrganizationType());
        if (request.getLegalName() != null)
            entity.setLegalName(request.getLegalName());
        if (request.getTradeName() != null)
            entity.setTradeName(request.getTradeName());
        if (request.getTaxNumber() != null)
            entity.setTaxNumber(request.getTaxNumber());
        if (request.getDerbisNumber() != null)
            entity.setDerbisNumber(request.getDerbisNumber());
        if (request.getMersisNumber() != null)
            entity.setMersisNumber(request.getMersisNumber());
        if (request.getEstablishmentDate() != null)
            entity.setEstablishmentDate(request.getEstablishmentDate());
        if (request.getDescription() != null)
            entity.setDescription(request.getDescription());
        if (request.getMissionStatement() != null)
            entity.setMissionStatement(request.getMissionStatement());
        if (request.getLogoUrl() != null)
            entity.setLogoUrl(request.getLogoUrl());
        if (request.getWebsiteUrl() != null)
            entity.setWebsiteUrl(request.getWebsiteUrl());
    }

    public OrganizationResponse toResponse(Organization entity) {
        return OrganizationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .organizationType(entity.getOrganizationType())
                .legalName(entity.getLegalName())
                .tradeName(entity.getTradeName())
                .taxNumber(entity.getTaxNumber())
                .logoUrl(entity.getLogoUrl())
                .websiteUrl(entity.getWebsiteUrl())
                .verificationStatus(entity.getVerificationStatus())
                .rejectionReason(entity.getRejectionReason())
                .verifiedAt(entity.getVerifiedAt())
                .verifiedBy(entity.getVerifiedBy() != null ? entity.getVerifiedBy().getId() : null)
                .isFeatured(entity.getFeatured())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDateTime() : null)
                .organizationTypeName(entity.getOrganizationType() != null ? entity.getOrganizationType().name() : null)
                .verificationStatusName(
                        entity.getVerificationStatus() != null ? entity.getVerificationStatus().name() : null)
                .description(entity.getDescription())
                .activeCampaignsCount(0) // Should be populated by service
                .totalRaised(BigDecimal.ZERO) // Should be populated by service
                .build();
    }

    public OrganizationDetailResponse toDetailResponse(Organization entity) {
        return OrganizationDetailResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .organizationType(entity.getOrganizationType())
                .legalName(entity.getLegalName())
                .tradeName(entity.getTradeName())
                .taxNumber(entity.getTaxNumber())
                .derbisNumber(entity.getDerbisNumber())
                .mersisNumber(entity.getMersisNumber())
                .establishmentDate(entity.getEstablishmentDate())
                .description(entity.getDescription())
                .missionStatement(entity.getMissionStatement())
                .logoUrl(entity.getLogoUrl())
                .websiteUrl(entity.getWebsiteUrl())
                .verificationStatus(entity.getVerificationStatus())
                .verifiedAt(entity.getVerifiedAt())
                .rejectionReason(entity.getRejectionReason())
                .resubmissionCount(entity.getResubmissionCount())
                .lastResubmissionAt(entity.getLastResubmissionAt())
                .isFeatured(entity.getFeatured())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDateTime() : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toLocalDateTime() : null)
                .contacts(toContactResponseList(entity.getContacts()))
                .documents(toDocumentResponseList(entity.getDocuments()))
                .bankAccounts(toBankAccountResponseList(entity.getBankAccounts()))
                .totalCampaigns(0L) // Service to populate
                .activeCampaigns(0L) // Service to populate
                .totalDonationsReceived(BigDecimal.ZERO) // Service to populate
                .transparencyScore(BigDecimal.ZERO)
                .build();
    }

    public OrganizationListResponse toListResponse(Organization entity) {
        return OrganizationListResponse.builder()
                .id(entity.getId())
                .organizationType(entity.getOrganizationType())
                .legalName(entity.getLegalName())
                .tradeName(entity.getTradeName())
                .logoUrl(entity.getLogoUrl())
                .verificationStatus(entity.getVerificationStatus())
                .isFeatured(entity.getFeatured())
                .transparencyScore(BigDecimal.ZERO)
                .campaignCount(0L)
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDateTime() : null)
                .build();
    }

    public OrganizationSummaryResponse toSummaryResponse(Organization entity) {
        return OrganizationSummaryResponse.builder()
                .id(entity.getId())
                .legalName(entity.getLegalName())
                .logoUrl(entity.getLogoUrl())
                .transparencyScore(BigDecimal.ZERO)
                .isVerified(entity.getVerificationStatus() == VerificationStatus.APPROVED)
                .build();
    }

    public List<OrganizationResponse> toResponseList(List<Organization> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<OrganizationListResponse> toListResponseList(List<Organization> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toListResponse).collect(Collectors.toList());
    }

    // Contact mappings
    public OrganizationContact toEntity(AddContactRequest request, Organization organization) {
        OrganizationContact contact = new OrganizationContact();
        contact.setOrganization(organization);
        contact.setContactType(request.getContactType());
        contact.setContactName(request.getContactName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setAddressLine1(request.getAddressLine1());
        contact.setAddressLine2(request.getAddressLine2());
        contact.setCity(request.getCity());
        contact.setDistrict(request.getDistrict());
        contact.setPostalCode(request.getPostalCode());
        contact.setCountry(request.getCountry());
        contact.setPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
        return contact;
    }

    public void updateEntity(OrganizationContact entity, UpdateContactRequest request) {
        if (request.getContactType() != null)
            entity.setContactType(request.getContactType());
        if (request.getContactName() != null)
            entity.setContactName(request.getContactName());
        if (request.getEmail() != null)
            entity.setEmail(request.getEmail());
        if (request.getPhone() != null)
            entity.setPhone(request.getPhone());
        if (request.getAddressLine1() != null)
            entity.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null)
            entity.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null)
            entity.setCity(request.getCity());
        if (request.getDistrict() != null)
            entity.setDistrict(request.getDistrict());
        if (request.getPostalCode() != null)
            entity.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null)
            entity.setCountry(request.getCountry());
        if (request.getIsPrimary() != null)
            entity.setPrimary(request.getIsPrimary());
    }

    public OrganizationContactResponse toResponse(OrganizationContact contact) {
        if (contact == null)
            return null;

        return OrganizationContactResponse.builder()
                .id(contact.getId())
                .contactType(contact.getContactType())
                .contactName(contact.getContactName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .addressLine1(contact.getAddressLine1())
                .addressLine2(contact.getAddressLine2())
                .city(contact.getCity())
                .district(contact.getDistrict())
                .postalCode(contact.getPostalCode())
                .country(contact.getCountry())
                .isPrimary(contact.getPrimary())
                .createdAt(contact.getCreatedAt() != null ? contact.getCreatedAt().toLocalDateTime() : null)
                .build();
    }

    public List<OrganizationContactResponse> toContactResponseList(List<OrganizationContact> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Document mappings
    public OrganizationDocument toEntity(AddDocumentRequest request, Organization organization) {
        OrganizationDocument document = new OrganizationDocument();
        document.setOrganization(organization);
        document.setDocumentType(request.getDocumentType());
        document.setDocumentName(request.getDocumentName());
        document.setFileUrl(request.getFileUrl());
        document.setFileSize(request.getFileSize());
        document.setMimeType(request.getMimeType());
        document.setExpiresAt(request.getExpiresAt());
        document.setVerified(false);
        // uploadedAt is handled by @PrePersist in entity
        return document;
    }

    public List<OrganizationDocumentResponse> toDocumentResponseList(List<OrganizationDocument> documents) {
        if (documents == null)
            return null;
        return documents.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public OrganizationDocumentResponse toResponse(OrganizationDocument document) {
        if (document == null)
            return null;

        String verifiedByName = null;
        if (document.getVerifiedBy() != null) {
            verifiedByName = document.getVerifiedBy().getFullName();
        }

        return OrganizationDocumentResponse.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .documentName(document.getDocumentName())
                .fileUrl(document.getFileUrl())
                .fileSize(document.getFileSize())
                .mimeType(document.getMimeType())
                .isVerified(document.getVerified())
                .verifiedAt(document.getVerifiedAt())
                .verifiedBy(verifiedByName)
                .expiresAt(document.getExpiresAt())
                .uploadedAt(document.getUploadedAt())
                .isExpiringSoon(isExpiringSoon(document.getExpiresAt()))
                .isExpired(isExpired(document.getExpiresAt()))
                .build();
    }

    // Bank account mappings
    public OrganizationBankAccount toEntity(AddBankAccountRequest request, Organization organization) {
        OrganizationBankAccount account = new OrganizationBankAccount();
        account.setOrganization(organization);
        account.setBankName(request.getBankName());
        account.setBankCode(request.getBankCode());
        account.setBranchName(request.getBranchName());
        account.setBranchCode(request.getBranchCode());
        account.setBranchCity(request.getBranchCity());
        account.setBranchDistrict(request.getBranchDistrict());
        account.setAccountHolder(request.getAccountHolder());
        account.setAccountNumber(request.getAccountNumber());
        account.setIban(request.getIban());
        account.setCurrency(request.getCurrency());
        account.setAccountType(request.getAccountType());
        account.setPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
        account.setVerified(false);
        return account;
    }

    public void updateEntity(OrganizationBankAccount entity, UpdateBankAccountRequest request) {
        if (request.getBankName() != null)
            entity.setBankName(request.getBankName());
        if (request.getBankCode() != null)
            entity.setBankCode(request.getBankCode());
        if (request.getBranchName() != null)
            entity.setBranchName(request.getBranchName());
        if (request.getBranchCode() != null)
            entity.setBranchCode(request.getBranchCode());
        if (request.getBranchCity() != null)
            entity.setBranchCity(request.getBranchCity());
        if (request.getBranchDistrict() != null)
            entity.setBranchDistrict(request.getBranchDistrict());
        if (request.getAccountHolder() != null)
            entity.setAccountHolder(request.getAccountHolder());
        if (request.getAccountNumber() != null)
            entity.setAccountNumber(request.getAccountNumber());
        if (request.getIban() != null)
            entity.setIban(request.getIban());
        if (request.getCurrency() != null)
            entity.setCurrency(request.getCurrency());
        if (request.getAccountType() != null)
            entity.setAccountType(request.getAccountType());
        if (request.getIsPrimary() != null)
            entity.setPrimary(request.getIsPrimary());
    }

    public OrganizationBankAccountResponse toResponse(OrganizationBankAccount entity) {
        return OrganizationBankAccountResponse.builder()
                .id(entity.getId())
                .bankName(entity.getBankName())
                .bankCode(entity.getBankCode())
                .branchName(entity.getBranchName())
                .branchCode(entity.getBranchCode())
                .branchCity(entity.getBranchCity())
                .branchDistrict(entity.getBranchDistrict())
                .accountHolder(entity.getAccountHolder())
                .accountNumber(entity.getAccountNumber())
                .iban(entity.getIban())
                .maskedIban(maskIban(entity.getIban()))
                .currency(entity.getCurrency())
                .accountType(entity.getAccountType())
                .isPrimary(entity.getPrimary())
                .isVerified(entity.getVerified())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDateTime() : null)
                .build();
    }

    public List<OrganizationBankAccountResponse> toBankAccountResponseList(List<OrganizationBankAccount> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Helper methods
    private String maskIban(String iban) {
        if (iban == null || iban.length() < 26)
            return iban;
        return "TR** **** **** **** **** **" + iban.substring(iban.length() - 2);
    }

    private boolean isExpired(LocalDate expiresAt) {
        if (expiresAt == null)
            return false;
        return expiresAt.isBefore(LocalDate.now());
    }

    private boolean isExpiringSoon(LocalDate expiresAt) {
        if (expiresAt == null)
            return false;
        LocalDate now = LocalDate.now();
        return !expiresAt.isBefore(now) && expiresAt.isBefore(now.plusDays(30));
    }
}
