package com.seffafbagis.api.dto.request.campaign;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateRealizationRequest {
    private String status;
    private LocalDateTime deadline;
}
