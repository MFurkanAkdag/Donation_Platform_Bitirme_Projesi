package com.seffafbagis.api.dto.response.donation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringDonationListResponse {

    private List<RecurringDonationResponse> items;
    private BigDecimal totalMonthlyAmount;
    private long totalActive;
}
