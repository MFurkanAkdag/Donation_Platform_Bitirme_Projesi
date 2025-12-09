package com.seffafbagis.api.service.admin;

import com.seffafbagis.api.dto.request.admin.AssignReportRequest;
import com.seffafbagis.api.dto.request.admin.ResolveReportRequest;
import com.seffafbagis.api.dto.response.admin.ReportResponse;
import com.seffafbagis.api.dto.response.admin.AdminOrganizationResponse.AdminUserSummary;
import com.seffafbagis.api.dto.response.common.PageResponse;
import com.seffafbagis.api.entity.report.Report;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.exception.ValidationException;
import com.seffafbagis.api.repository.ReportRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.service.audit.AuditLogService;
import com.seffafbagis.api.service.notification.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    public AdminReportService(ReportRepository reportRepository,
            UserRepository userRepository,
            EmailService emailService,
            AuditLogService auditLogService) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
    }

    public PageResponse<ReportResponse> getAllReports(Pageable pageable) {
        Page<Report> reports = reportRepository.findAll(pageable);
        List<ReportResponse> content = reports.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PageResponse.of(content, reports);
    }

    public PageResponse<ReportResponse> getPendingReports(Pageable pageable) {
        Page<Report> reports = reportRepository.findByStatusIn(Arrays.asList("PENDING", "ASSIGNED"), pageable);
        List<ReportResponse> content = reports.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PageResponse.of(content, reports);
    }

    public PageResponse<ReportResponse> getReportsByPriority(String priority, Pageable pageable) {
        Page<Report> reports = reportRepository.findByPriority(priority, pageable);
        List<ReportResponse> content = reports.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PageResponse.of(content, reports);
    }

    public ReportResponse getReportById(UUID id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", id.toString()));
        return mapToResponse(report);
    }

    public ReportResponse assignReport(UUID id, AssignReportRequest request, UUID adminId) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", id.toString()));

        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId().toString(),
                        "Assignee not found"));

        if (!assignee.getRole().equals(UserRole.ADMIN)) {
            // Check if UserRole is correct enum. Assuming UserRole has ADMIN.
            // If stricter check needed:
            // throw new ValidationException("Assignee must be an admin");
        }

        report.setAssignedTo(assignee);
        report.setAssignedAt(LocalDateTime.now());
        if (request.getPriority() != null) {
            report.setPriority(request.getPriority());
        }
        report.setStatus("ASSIGNED");

        reportRepository.save(report);

        auditLogService.logAction(adminId, "ASSIGN_REPORT",
                "Assigned report " + id + " to " + assignee.getEmail(), id.toString());

        // Notify assignee logic...

        return mapToResponse(report);
    }

    public ReportResponse resolveReport(UUID id, ResolveReportRequest request, UUID adminId) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", id.toString()));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adminId.toString(), "Admin not found"));

        report.setResolution(request.getResolution());
        report.setResolutionNotes(request.getResolutionNotes());
        report.setResolvedBy(admin);
        report.setResolvedAt(LocalDateTime.now());
        report.setStatus(request.getResolution());

        reportRepository.save(report);

        auditLogService.logAction(adminId, "RESOLVE_REPORT",
                "Resolved report " + id + " with status " + request.getResolution(), id.toString());

        if (request.getNotifyReporter() != null && request.getNotifyReporter()) {
            // emailService.sendReportResolutionEmail(...);
        }

        return mapToResponse(report);
    }

    // Statistics method placeholder
    // public ReportStatistics getReportStatistics() { ... }

    private ReportResponse mapToResponse(Report report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());
        response.setReporterId(report.getReporter().getId());
        response.setReporterEmail(report.getReporter().getEmail());
        response.setReportType(report.getReportType());
        response.setTargetType(report.getTargetType());
        response.setTargetId(report.getTargetId());
        response.setTargetName(report.getTargetName());
        response.setReason(report.getReason());
        response.setDescription(report.getDescription());
        response.setStatus(report.getStatus());
        response.setPriority(report.getPriority());

        if (report.getAssignedTo() != null) {
            response.setAssignedTo(new AdminUserSummary(
                    report.getAssignedTo().getId(),
                    report.getAssignedTo().getEmail(),
                    report.getAssignedTo().getProfile() != null ? report.getAssignedTo().getProfile().getDisplayName()
                            : null));
        }
        response.setAssignedAt(report.getAssignedAt());

        response.setResolution(report.getResolution());
        response.setResolutionNotes(report.getResolutionNotes());

        if (report.getResolvedBy() != null) {
            response.setResolvedBy(new AdminUserSummary(
                    report.getResolvedBy().getId(),
                    report.getResolvedBy().getEmail(),
                    report.getResolvedBy().getProfile() != null ? report.getResolvedBy().getProfile().getDisplayName()
                            : null));
        }
        response.setResolvedAt(report.getResolvedAt());
        response.setCreatedAt(report.getCreatedAt().toLocalDateTime());

        return response;
    }
}
