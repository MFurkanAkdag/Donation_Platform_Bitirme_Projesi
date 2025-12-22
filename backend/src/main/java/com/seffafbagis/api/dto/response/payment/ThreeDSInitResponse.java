package com.seffafbagis.api.dto.response.payment;

public class ThreeDSInitResponse {
    private String threeDSHtmlContent;
    private String paymentId;

    public ThreeDSInitResponse() {
    }

    public ThreeDSInitResponse(String threeDSHtmlContent, String paymentId) {
        this.threeDSHtmlContent = threeDSHtmlContent;
        this.paymentId = paymentId;
    }

    public String getThreeDSHtmlContent() {
        return threeDSHtmlContent;
    }

    public void setThreeDSHtmlContent(String threeDSHtmlContent) {
        this.threeDSHtmlContent = threeDSHtmlContent;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
}
