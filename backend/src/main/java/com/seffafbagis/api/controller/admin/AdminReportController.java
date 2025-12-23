package com.seffafbagis.api.controller.admin;

import com.seffafbagis.api.dto.request.admin.AssignReportRequest;
import com.seffafbagis.api.dto.request.admin.ResolveReportRequest;
import com.seffafbagis.api.dto.response.admin.ReportResponse;
import com.seffafbagis.api.dto.response.common.PageResponse;
import com.seffafbagis.api.security.CustomUserDetails;
import com.seffafbagis.api.service.admin.AdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reports")
@Tag(name = "Admin - Reports", description = "Admin report/complaint management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final AdminReportService adminReportService;

    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    @GetMapping
    @Operation(summary = "List all reports")
    public ResponseEntity<PageResponse<ReportResponse>> getAllReports(Pageable pageable) {
        return ResponseEntity.ok(adminReportService.getAllReports(pageable));
    }

    @GetMapping("/pending")
    @Operation(summary = "List pending reports")
    public ResponseEntity<PageResponse<ReportResponse>> getPendingReports(Pageable pageable) {
        return ResponseEntity.ok(adminReportService.getPendingReports(pageable));
    }

    @GetMapping("/priority/{priority}")
    @Operation(summary = "List reports by priority")
    public ResponseEntity<PageResponse<ReportResponse>> getReportsByPriority(
            @PathVariable String priority, Pageable pageable) {
        com.seffafbagis.api.enums.ReportPriority priorityEnum = com.seffafbagis.api.enums.ReportPriority
                .valueOf(priority.toUpperCase());
        return ResponseEntity.ok(adminReportService.getReportsByPriority(priorityEnum, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get report details")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminReportService.getReportById(id));
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign report to admin")
    public ResponseEntity<ReportResponse> assignReport(
            @PathVariable UUID id,
            @Valid @RequestBody AssignReportRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(adminReportService.assignReport(id, request, currentUser.getId()));
    }

    @PutMapping("/{id}/resolve")
    @Operation(summary = "Resolve report")
    public ResponseEntity<ReportResponse> resolveReport(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveReportRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(adminReportService.resolveReport(id, request, currentUser.getId()));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get report statistics")
    public ResponseEntity<Object> getReportStatistics() {
        // Placeholder as service method wasn't explicitly required/implemented
        return ResponseEntity.ok().build();
    }
}
