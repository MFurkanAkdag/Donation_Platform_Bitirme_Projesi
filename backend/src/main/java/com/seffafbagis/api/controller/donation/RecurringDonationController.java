package com.seffafbagis.api.controller.donation;

import com.seffafbagis.api.dto.request.donation.CreateRecurringDonationRequest;
import com.seffafbagis.api.dto.request.donation.UpdateRecurringDonationRequest;
import com.seffafbagis.api.dto.response.donation.RecurringDonationListResponse;
import com.seffafbagis.api.dto.response.donation.RecurringDonationResponse;
import com.seffafbagis.api.service.donation.RecurringDonationService;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recurring-donations")
@RequiredArgsConstructor
@Tag(name = "Recurring Donations", description = "Endpoints for managing recurring donation subscriptions")
public class RecurringDonationController {

    private final RecurringDonationService recurringDonationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create recurring donation", description = "Create a new recurring donation subscription (weekly/monthly/yearly)")
    public ResponseEntity<ApiResponse<RecurringDonationResponse>> createRecurringDonation(
            @Valid @RequestBody CreateRecurringDonationRequest request) {
        RecurringDonationResponse response = recurringDonationService.createRecurringDonation(request);
        return ResponseEntity.ok(ApiResponse.success("Recurring donation created successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my recurring donations", description = "List all recurring donations for the current user")
    public ResponseEntity<ApiResponse<RecurringDonationListResponse>> getMyRecurringDonations() {
        RecurringDonationListResponse response = recurringDonationService.getMyRecurringDonations();
        return ResponseEntity.ok(ApiResponse.success("Recurring donations retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get recurring donation details", description = "Get details of a specific recurring donation")
    public ResponseEntity<ApiResponse<RecurringDonationResponse>> getRecurringDonation(@PathVariable UUID id) {
        RecurringDonationResponse response = recurringDonationService.getRecurringDonation(id);
        return ResponseEntity.ok(ApiResponse.success("Recurring donation details retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update recurring donation", description = "Update amount or frequency of a recurring donation")
    public ResponseEntity<ApiResponse<RecurringDonationResponse>> updateRecurringDonation(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRecurringDonationRequest request) {
        RecurringDonationResponse response = recurringDonationService.updateRecurringDonation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Recurring donation updated successfully", response));
    }

    @PutMapping("/{id}/pause")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Pause recurring donation", description = "Pause a recurring donation")
    public ResponseEntity<ApiResponse<Void>> pauseRecurringDonation(@PathVariable UUID id) {
        recurringDonationService.pauseRecurringDonation(id);
        return ResponseEntity.ok(ApiResponse.success("Recurring donation paused successfully", null));
    }

    @PutMapping("/{id}/resume")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Resume recurring donation", description = "Resume a paused recurring donation")
    public ResponseEntity<ApiResponse<Void>> resumeRecurringDonation(@PathVariable UUID id) {
        recurringDonationService.resumeRecurringDonation(id);
        return ResponseEntity.ok(ApiResponse.success("Recurring donation resumed successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel recurring donation", description = "Cancel (delete) a recurring donation")
    public ResponseEntity<ApiResponse<Void>> cancelRecurringDonation(@PathVariable UUID id) {
        recurringDonationService.cancelRecurringDonation(id);
        return ResponseEntity.ok(ApiResponse.success("Recurring donation cancelled successfully", null));
    }
}
