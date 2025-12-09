package com.seffafbagis.api.dto.response.organization;

public class OrganizationStatistics {
    private Long totalOrganizations;
    private Long pendingVerifications;
    private Long verifiedOrganizations;
    private Long rejectedOrganizations;

    public OrganizationStatistics() {
    }

    public Long getTotalOrganizations() {
        return totalOrganizations;
    }

    public void setTotalOrganizations(Long totalOrganizations) {
        this.totalOrganizations = totalOrganizations;
    }

    public Long getPendingVerifications() {
        return pendingVerifications;
    }

    public void setPendingVerifications(Long pendingVerifications) {
        this.pendingVerifications = pendingVerifications;
    }

    public Long getVerifiedOrganizations() {
        return verifiedOrganizations;
    }

    public void setVerifiedOrganizations(Long verifiedOrganizations) {
        this.verifiedOrganizations = verifiedOrganizations;
    }

    public Long getRejectedOrganizations() {
        return rejectedOrganizations;
    }

    public void setRejectedOrganizations(Long rejectedOrganizations) {
        this.rejectedOrganizations = rejectedOrganizations;
    }
}
