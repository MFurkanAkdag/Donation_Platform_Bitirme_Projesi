package com.seffafbagis.api.dto.response.user;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for masked sensitive data (KVKK compliant).
 * Never returns full sensitive data.
 */
public class UserSensitiveDataResponse {

    private UUID id;
    private Boolean hasTcKimlik;
    private String tcKimlikMasked;
    private Boolean hasPhone;
    private String phoneMasked;
    private Boolean hasAddress;
    private String addressPreview;
    private Boolean hasBirthDate;
    private Integer birthYear;
    private Boolean dataProcessingConsent;
    private LocalDateTime consentDate;
    private Boolean marketingConsent;
    private LocalDateTime marketingConsentDate;
    private Boolean thirdPartySharingConsent;
    private LocalDateTime thirdPartySharingConsentDate;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Boolean getHasTcKimlik() {
        return hasTcKimlik;
    }

    public void setHasTcKimlik(Boolean hasTcKimlik) {
        this.hasTcKimlik = hasTcKimlik;
    }

    public String getTcKimlikMasked() {
        return tcKimlikMasked;
    }

    public void setTcKimlikMasked(String tcKimlikMasked) {
        this.tcKimlikMasked = tcKimlikMasked;
    }

    public Boolean getHasPhone() {
        return hasPhone;
    }

    public void setHasPhone(Boolean hasPhone) {
        this.hasPhone = hasPhone;
    }

    public String getPhoneMasked() {
        return phoneMasked;
    }

    public void setPhoneMasked(String phoneMasked) {
        this.phoneMasked = phoneMasked;
    }

    public Boolean getHasAddress() {
        return hasAddress;
    }

    public void setHasAddress(Boolean hasAddress) {
        this.hasAddress = hasAddress;
    }

    public String getAddressPreview() {
        return addressPreview;
    }

    public void setAddressPreview(String addressPreview) {
        this.addressPreview = addressPreview;
    }

    public Boolean getHasBirthDate() {
        return hasBirthDate;
    }

    public void setHasBirthDate(Boolean hasBirthDate) {
        this.hasBirthDate = hasBirthDate;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    public Boolean getDataProcessingConsent() {
        return dataProcessingConsent;
    }

    public void setDataProcessingConsent(Boolean dataProcessingConsent) {
        this.dataProcessingConsent = dataProcessingConsent;
    }

    public LocalDateTime getConsentDate() {
        return consentDate;
    }

    public void setConsentDate(LocalDateTime consentDate) {
        this.consentDate = consentDate;
    }

    public Boolean getMarketingConsent() {
        return marketingConsent;
    }

    public void setMarketingConsent(Boolean marketingConsent) {
        this.marketingConsent = marketingConsent;
    }

    public LocalDateTime getMarketingConsentDate() {
        return marketingConsentDate;
    }

    public void setMarketingConsentDate(LocalDateTime marketingConsentDate) {
        this.marketingConsentDate = marketingConsentDate;
    }

    public Boolean getThirdPartySharingConsent() {
        return thirdPartySharingConsent;
    }

    public void setThirdPartySharingConsent(Boolean thirdPartySharingConsent) {
        this.thirdPartySharingConsent = thirdPartySharingConsent;
    }

    public LocalDateTime getThirdPartySharingConsentDate() {
        return thirdPartySharingConsentDate;
    }

    public void setThirdPartySharingConsentDate(LocalDateTime thirdPartySharingConsentDate) {
        this.thirdPartySharingConsentDate = thirdPartySharingConsentDate;
    }
}
