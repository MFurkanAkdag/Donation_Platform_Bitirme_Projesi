package com.seffafbagis.api.dto.response.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationListResponse {

    private List<ApplicationResponse> applications;
    private long totalCount;
    private long pendingCount;
    private long approvedCount;
}
