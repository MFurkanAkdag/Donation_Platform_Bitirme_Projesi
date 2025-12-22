package com.seffafbagis.api.dto.request.evidence;

import com.seffafbagis.api.enums.EvidenceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateEvidenceRequest {

    private EvidenceType evidenceType;

    @Size(max = 255)
    private String title;

    private String description;

    @DecimalMin("0.01")
    private BigDecimal amountSpent;

    @PastOrPresent
    private LocalDate spendDate;

    @Size(max = 255)
    private String vendorName;

    @Size(max = 20)
    private String vendorTaxNumber;

    @Size(max = 100)
    private String invoiceNumber;

    private List<CreateEvidenceDocumentRequest> documents;
}
