package com.seffafbagis.api.dto.request.user;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating KVKK consent settings.
 */
public class UpdateConsentRequest {

    @NotNull(message = "Marketing consent is required")
    private Boolean marketingConsent;

    @NotNull(message = "Third party sharing consent is required")
    private Boolean thirdPartySharingConsent;

    public Boolean getMarketingConsent() {
        return marketingConsent;
    }

    public void setMarketingConsent(Boolean marketingConsent) {
        this.marketingConsent = marketingConsent;
    }

    public Boolean getThirdPartySharingConsent() {
        return thirdPartySharingConsent;
    }

    public void setThirdPartySharingConsent(Boolean thirdPartySharingConsent) {
        this.thirdPartySharingConsent = thirdPartySharingConsent;
    }
}
