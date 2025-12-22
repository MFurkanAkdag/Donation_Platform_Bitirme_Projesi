package com.seffafbagis.api.dto.response.application;

import com.seffafbagis.api.enums.ApplicationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ApplicationResponse {

    private UUID id;
    private UUID applicantId;
    private UUID categoryId;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal requestedAmount;
    private ApplicationStatus status;
    private String locationCity;
    private String locationDistrict;
    private Integer householdSize;
    private Integer urgencyLevel;
    private OffsetDateTime createdAt;
}
