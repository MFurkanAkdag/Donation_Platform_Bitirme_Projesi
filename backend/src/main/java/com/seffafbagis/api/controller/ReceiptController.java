package com.seffafbagis.api.controller;

import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.ReceiptVerificationResponse;
import com.seffafbagis.api.entity.Receipt;
import com.seffafbagis.api.service.receipt.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @GetMapping("/verify/{barcode}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<ReceiptVerificationResponse>> verifyReceipt(@PathVariable String barcode) {
        return receiptService.verifyReceipt(barcode)
                .map(receipt -> {
                    ReceiptVerificationResponse response = ReceiptVerificationResponse.builder()
                            .valid(true)
                            .receiptCode(receipt.getBarcodeData())
                            .donationAmount(receipt.getDonation().getAmount())
                            .donorName(mockMaskName(receipt.getDonation().getDonorDisplayName()))
                            .date(receipt.getDonation().getCreatedAt().toLocalDateTime())
                            .campaignTitle(receipt.getDonation().getCampaign().getTitle())
                            .build();
                    return ResponseEntity.ok(ApiResponse.success(response));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error("Invalid Receipt Barcode", "NOT_FOUND")));
    }

    private String mockMaskName(String name) {
        if (name == null || name.length() < 2)
            return "***";
        return name.charAt(0) + "***" + name.charAt(name.length() - 1);
    }
}
