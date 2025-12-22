package com.seffafbagis.api.dto.response.donation;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DonationDetailResponse extends DonationResponse {
    private String refundStatus;
    private String refundReason;
    private DonationReceiptResponse receipt;
    // Transaction details can be added here if needed, or kept separate
}
