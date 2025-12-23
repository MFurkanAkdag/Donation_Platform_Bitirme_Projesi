package com.seffafbagis.api.dto.response.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationStatistics {
    private Long totalOrganizations;
    private Long pendingVerifications;
    private Long verifiedOrganizations;
    private Long rejectedOrganizations;
}
