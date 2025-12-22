package com.seffafbagis.api.dto.request.application;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateApplicationRequest {

    private UUID categoryId;

    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    private String description;

    private BigDecimal requestedAmount;

    private String locationCity;

    private String locationDistrict;

    private Integer householdSize;

    private Integer urgencyLevel;
}
