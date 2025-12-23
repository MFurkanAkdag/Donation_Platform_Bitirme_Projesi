package com.seffafbagis.api.controller.donation;

import com.seffafbagis.api.dto.request.donation.InitiateBankTransferRequest;
import com.seffafbagis.api.dto.request.donation.MatchBankTransferRequest;
import com.seffafbagis.api.dto.response.donation.BankTransferInfoResponse;
import com.seffafbagis.api.dto.response.donation.BankTransferReferenceResponse;
import com.seffafbagis.api.service.donation.BankTransferService;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Bank Transfers", description = "Endpoints for bank transfer donations (Havale/EFT)")
public class BankTransferController {

    private final BankTransferService bankTransferService;

    // User Endpoints

    @PostMapping("/bank-transfers/initiate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Initiate bank transfer", description = "Generate a reference code and get bank account details for transfer")
    public ResponseEntity<ApiResponse<BankTransferInfoResponse>> initiateBankTransfer(
            @Valid @RequestBody InitiateBankTransferRequest request) {
        BankTransferInfoResponse response = bankTransferService.initiateBankTransfer(request);
        return ResponseEntity.ok(ApiResponse.success("Bank transfer initiated successfully", response));
    }

    @GetMapping("/bank-transfers/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "My pending transfers", description = "Get list of my pending bank transfer references")
    public ResponseEntity<ApiResponse<List<BankTransferReferenceResponse>>> getMyPendingTransfers() {
        List<BankTransferReferenceResponse> response = bankTransferService.getMyPendingReferences();
        return ResponseEntity.ok(ApiResponse.success("Pending bank transfers retrieved successfully", response));
    }

    @GetMapping("/bank-transfers/{code}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get transfer status", description = "Check status of a bank transfer reference")
    public ResponseEntity<ApiResponse<BankTransferReferenceResponse>> getBankTransferStatus(@PathVariable String code) {
        BankTransferReferenceResponse response = bankTransferService.getBankTransferStatus(code);
        return ResponseEntity.ok(ApiResponse.success("Bank transfer status retrieved successfully", response));
    }

    @DeleteMapping("/bank-transfers/{code}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel transfer", description = "Cancel a pending bank transfer reference")
    public ResponseEntity<ApiResponse<Void>> cancelBankTransfer(@PathVariable String code) {
        bankTransferService.cancelBankTransfer(code);
        return ResponseEntity.ok(ApiResponse.success("Bank transfer cancelled successfully", null));
    }

    // Admin Endpoints

    @GetMapping("/admin/bank-transfers/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pending transfers (Admin)", description = "List all pending bank transfers for matching")
    public ResponseEntity<ApiResponse<Page<BankTransferReferenceResponse>>> getPendingBankTransfers(Pageable pageable) {
        Page<BankTransferReferenceResponse> response = bankTransferService.getPendingBankTransfers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending bank transfers retrieved successfully", response));
    }

    @PostMapping("/admin/bank-transfers/match")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Match transfer (Admin)", description = "Manually match a bank transfer to a donation")
    public ResponseEntity<ApiResponse<Void>> matchBankTransfer(
            @Valid @RequestBody MatchBankTransferRequest request) {
        bankTransferService.matchBankTransfer(request);
        return ResponseEntity.ok(ApiResponse.success("Bank transfer matched successfully", null));
    }

    @PutMapping("/admin/bank-transfers/{code}/expire")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Expire transfer (Admin)", description = "Manually expire a bank transfer reference")
    public ResponseEntity<ApiResponse<Void>> expireBankTransfer(@PathVariable String code) {
        bankTransferService.expireBankTransfer(code);
        return ResponseEntity.ok(ApiResponse.success("Bank transfer expired successfully", null));
    }
}
