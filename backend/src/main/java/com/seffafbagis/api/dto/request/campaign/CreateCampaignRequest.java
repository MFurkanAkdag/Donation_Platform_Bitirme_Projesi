package com.seffafbagis.api.dto.request.campaign;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for creating a campaign.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCampaignRequest {

    @NotBlank(message = "Başlık gereklidir")
    @Size(max = 200, message = "Başlık en fazla 200 karakter olabilir")
    private String title;

    @Size(max = 500, message = "Özet en fazla 500 karakter olabilir")
    private String summary;

    @NotBlank(message = "Açıklama gereklidir")
    private String description;

    @Size(max = 500, message = "Kısa açıklama en fazla 500 karakter olabilir")
    private String shortDescription;

    @NotNull(message = "Hedef tutar gereklidir")
    @DecimalMin(value = "100.00", message = "Minimum hedef tutar 100 TL")
    private BigDecimal targetAmount;

    private String currency;

    private LocalDate startDate;

    private LocalDate endDate;

    private String coverImageUrl;

    private Integer evidenceDeadlineDays;

    private Boolean isUrgent;

    private String locationCity;

    private String locationDistrict;

    private Integer beneficiaryCount;

    private List<UUID> categoryIds;

    private List<UUID> donationTypeIds;
}
