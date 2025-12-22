package com.seffafbagis.api.dto.request.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreeDSCallbackRequest {
    private String status;
    private String paymentId;
    private String conversationId;
    private String mdStatus;
}
