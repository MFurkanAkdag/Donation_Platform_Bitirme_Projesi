package com.seffafbagis.api.controller;

import com.seffafbagis.api.dto.request.organization.AddBankAccountRequest;
import com.seffafbagis.api.dto.request.organization.UpdateBankAccountRequest;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationBankAccountResponse;
import com.seffafbagis.api.service.organization.OrganizationBankAccountService;
import com.seffafbagis.api.service.organization.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organization/bank-accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FOUNDATION')")
public class OrganizationBankAccountController {

    private final OrganizationBankAccountService bankAccountService;
    private final OrganizationService organizationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationBankAccountResponse>>> getBankAccounts() {
        UUID orgId = organizationService.getMyOrganization().getId();
        List<OrganizationBankAccountResponse> accounts = bankAccountService.getBankAccounts(orgId);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationBankAccountResponse>> addBankAccount(
            @Valid @RequestBody AddBankAccountRequest request) {
        OrganizationBankAccountResponse response = bankAccountService.addBankAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationBankAccountResponse>> updateBankAccount(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBankAccountRequest request) {
        OrganizationBankAccountResponse response = bankAccountService.updateBankAccount(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBankAccount(@PathVariable UUID id) {
        bankAccountService.deleteBankAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Bank account deleted successfully", null));
    }

    @PutMapping("/{id}/primary")
    public ResponseEntity<ApiResponse<OrganizationBankAccountResponse>> setPrimaryBankAccount(@PathVariable UUID id) {
        OrganizationBankAccountResponse response = bankAccountService.setPrimaryBankAccount(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
