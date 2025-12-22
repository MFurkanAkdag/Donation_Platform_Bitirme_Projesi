package com.seffafbagis.api.dto.response.donation;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DonorListResponse {
    private String donorDisplayName;
    private BigDecimal amount;
    private String message;
    private LocalDateTime createdAt;
}
