package com.seffafbagis.api.dto.request.organization;

import com.seffafbagis.api.enums.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequest {

    @NotNull(message = "Organization type is required")
    private OrganizationType organizationType;

    @NotBlank(message = "Legal name is required")
    @Size(max = 255, message = "Legal name must not exceed 255 characters")
    private String legalName;

    @Size(max = 255, message = "Trade name must not exceed 255 characters")
    private String tradeName;

    @Size(max = 20, message = "Tax number must not exceed 20 characters")
    private String taxNumber;

    @Size(max = 50, message = "DERBİS number must not exceed 50 characters")
    private String derbisNumber;

    @Size(max = 50, message = "MERSİS number must not exceed 50 characters")
    private String mersisNumber;

    private LocalDate establishmentDate;

    private String description;

    private String missionStatement;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "Invalid website URL format")
    private String websiteUrl;
}
