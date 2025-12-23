package com.seffafbagis.api.dto.request.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO for creating an application (beneficiary request).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApplicationRequest {

    @NotNull(message = "Kategori ID gereklidir")
    private UUID categoryId;

    @NotBlank(message = "Başlık gereklidir")
    private String title;

    @NotBlank(message = "Açıklama gereklidir")
    private String description;

    @NotNull(message = "Talep edilen tutar gereklidir")
    private BigDecimal requestedAmount;

    private String locationCity;

    private String locationDistrict;

    private Integer householdSize;

    private Integer urgencyLevel;

    // Documents for the application
    private List<DocumentRequest> documents;

    // Legacy fields for backward compatibility
    private UUID campaignId;
    private String applicationType;
    private String firstName;
    private String lastName;
    private String identityNumber;
    private String phoneNumber;
    private String email;
    private String address;
    private String incomeLevel;
}
