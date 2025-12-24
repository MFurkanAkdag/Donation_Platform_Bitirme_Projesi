package com.seffafbagis.api.controller.report;

import com.seffafbagis.api.dto.request.report.AssignReportRequest;
import com.seffafbagis.api.dto.request.report.CreateReportRequest;
import com.seffafbagis.api.dto.request.report.ResolveReportRequest;
import com.seffafbagis.api.dto.request.report.UpdateReportPriorityRequest;
import com.seffafbagis.api.dto.response.report.ReportDetailResponse;
import com.seffafbagis.api.dto.response.report.ReportResponse;
import com.seffafbagis.api.dto.response.report.ReportStatsResponse;
import com.seffafbagis.api.enums.ReportEntityType;
import com.seffafbagis.api.enums.ReportStatus;
import com.seffafbagis.api.enums.ReportType;
import com.seffafbagis.api.service.report.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // --- Public & User Endpoints ---

    @PostMapping("/reports/anonymous")
    public ResponseEntity<ReportResponse> createAnonymousReport(@Valid @RequestBody CreateReportRequest request) {
        request.setAnonymous(true);
        return ResponseEntity.ok(reportService.createReport(request));
    }

    @PostMapping("/reports")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody CreateReportRequest request) {
        request.setAnonymous(false);
        return ResponseEntity.ok(reportService.createReport(request));
    }

    @GetMapping("/reports/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ReportResponse>> getMyReports(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reportService.getMyReports(pageable));
    }

    @GetMapping("/reports/my/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportDetailResponse> getMyReport(@PathVariable UUID id) {
        return ResponseEntity.ok(reportService.getMyReport(id));
    }

    // --- Admin Endpoints ---

    /*
     * @GetMapping("/admin/reports")
     * 
     * @PreAuthorize("hasRole('ADMIN')")
     * public ResponseEntity<Page<ReportResponse>> getAllReports(
     * 
     * @RequestParam(required = false) ReportStatus status,
     * 
     * @RequestParam(required = false) ReportType type,
     * 
     * @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
     * Pageable pageable) {
     * return ResponseEntity.ok(reportService.getAllReports(status, type,
     * pageable));
     * }
     */

    /*
     * @GetMapping("/admin/reports/pending")
     * 
     * @PreAuthorize("hasRole('ADMIN')")
     * public ResponseEntity<Page<ReportResponse>> getPendingReports(
     * 
     * @PageableDefault(size = 20) Pageable pageable) {
     * return ResponseEntity.ok(reportService.getPendingReports(pageable));
     * }
     */

    @GetMapping("/admin/reports/assigned")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReportResponse>> getMyAssignedReports(
            @PageableDefault(sort = "assignedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reportService.getMyAssignedReports(pageable));
    }

    /*
     * @GetMapping("/admin/reports/{id}")
     * 
     * @PreAuthorize("hasRole('ADMIN')")
     * public ResponseEntity<ReportDetailResponse> getReportDetail(@PathVariable
     * UUID id) {
     * return ResponseEntity.ok(reportService.getReportDetail(id));
     * }
     */

    /*
     * @PostMapping("/admin/reports/{id}/assign")
     * 
     * @PreAuthorize("hasRole('ADMIN')")
     * public ResponseEntity<Void> assignReport(
     * 
     * @PathVariable UUID id,
     * 
     * @Valid @RequestBody AssignReportRequest request) {
     * reportService.assignReport(id, request);
     * return ResponseEntity.ok().build();
     * }
     */

    @PostMapping("/admin/reports/{id}/investigate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> startInvestigation(@PathVariable UUID id) {
        reportService.startInvestigation(id);
        return ResponseEntity.ok().build();
    }

    /*
     * @PostMapping("/admin/reports/{id}/resolve")
     * 
     * @PreAuthorize("hasRole('ADMIN')")
     * public ResponseEntity<Void> resolveReport(
     * 
     * @PathVariable UUID id,
     * 
     * @Valid @RequestBody ResolveReportRequest request) {
     * reportService.resolveReport(id, request);
     * return ResponseEntity.ok().build();
     * }
     */

    @PutMapping("/admin/reports/{id}/priority")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePriority(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReportPriorityRequest request) {
        reportService.updatePriority(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/reports/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportStatsResponse> getStats() {
        return ResponseEntity.ok(reportService.getStats());
    }

    @GetMapping("/admin/reports/entity/{type}/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReportResponse>> getEntityReports(
            @PathVariable ReportEntityType type,
            @PathVariable UUID id,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reportService.getEntityReports(type, id, pageable));
    }
}
