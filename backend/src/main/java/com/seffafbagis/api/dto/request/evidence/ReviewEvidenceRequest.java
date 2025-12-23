package com.seffafbagis.api.dto.request.evidence;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for reviewing evidence (admin approval/rejection).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEvidenceRequest {

    @NotNull(message = "Onay durumu gereklidir")
    private Boolean approved;

    private String rejectionReason;

    private String notes;
}
