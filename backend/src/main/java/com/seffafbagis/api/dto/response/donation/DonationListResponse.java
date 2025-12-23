package com.seffafbagis.api.dto.response.donation;

import org.springframework.data.domain.Page;
import java.math.BigDecimal;

public class DonationListResponse {
    private Page<DonationResponse> donations;
    private BigDecimal totalAmount;
    private long totalCount;

    public DonationListResponse() {
    }

    public DonationListResponse(Page<DonationResponse> donations, BigDecimal totalAmount, long totalCount) {
        this.donations = donations;
        this.totalAmount = totalAmount;
        this.totalCount = totalCount;
    }

    public Page<DonationResponse> getDonations() {
        return donations;
    }

    public void setDonations(Page<DonationResponse> donations) {
        this.donations = donations;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
