package com.seffafbagis.api.dto.request.evidence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO for creating evidence.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEvidenceRequest {

    @NotNull(message = "Kampanya ID gereklidir")
    private UUID campaignId;

    @NotBlank(message = "Kanıt türü gereklidir")
    private String evidenceType;

    @NotBlank(message = "Başlık gereklidir")
    private String title;

    private String description;

    @NotNull(message = "Harcanan tutar gereklidir")
    private BigDecimal amountSpent;

    @NotNull(message = "Harcama tarihi gereklidir")
    private LocalDate spendDate;

    private String vendorName;

    private String vendorTaxNumber;

    private String invoiceNumber;

    private List<UUID> documentIds;
}
