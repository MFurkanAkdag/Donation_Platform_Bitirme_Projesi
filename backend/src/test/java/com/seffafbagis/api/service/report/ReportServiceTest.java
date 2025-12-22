package com.seffafbagis.api.service.report;

import com.seffafbagis.api.dto.mapper.ReportMapper;
import com.seffafbagis.api.dto.request.report.CreateReportRequest;
import com.seffafbagis.api.dto.request.report.ResolveReportRequest;
import com.seffafbagis.api.dto.response.report.ReportResponse;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.report.Report;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.ReportEntityType;
import com.seffafbagis.api.enums.ReportPriority;
import com.seffafbagis.api.enums.ReportStatus;
import com.seffafbagis.api.enums.ReportType;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.ReportRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.SecurityUtils;
import com.seffafbagis.api.service.notification.NotificationService;
import com.seffafbagis.api.service.transparency.TransparencyScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;
    @Mock
    private ReportMapper reportMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private TransparencyScoreService transparencyScoreService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReportService reportService;

    private User reporter;
    private Report report;
    private CreateReportRequest createRequest;
    private UUID reporterId;
    private UUID entityId;

    @BeforeEach
    void setUp() {
        reporterId = UUID.randomUUID();
        entityId = UUID.randomUUID();

        reporter = new User();
        reporter.setId(reporterId);

        createRequest = new CreateReportRequest();
        createRequest.setReportType(ReportType.FRAUD);
        createRequest.setEntityType(ReportEntityType.ORGANIZATION);
        createRequest.setEntityId(entityId);
        createRequest.setReason("Fraud activity");
        createRequest.setAnonymous(false);

        report = new Report();
        report.setId(UUID.randomUUID());
        report.setReportType(ReportType.FRAUD);
        report.setEntityType(ReportEntityType.ORGANIZATION);
        report.setEntityId(entityId);
        report.setStatus(ReportStatus.PENDING);
        report.setReporter(reporter);
    }

    @Test
    void createReport_WhenValid_ShouldCreateReport() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(reporterId));

            when(userRepository.getReferenceById(reporterId)).thenReturn(reporter);
            when(organizationRepository.existsById(entityId)).thenReturn(true);
            when(reportRepository.existsByReporterIdAndEntityTypeAndEntityIdAndStatusNot(any(), any(), any(), any()))
                    .thenReturn(false);
            when(reportMapper.toEntity(any(), any())).thenReturn(report);
            when(reportRepository.save(any(Report.class))).thenReturn(report);
            when(reportMapper.toResponse(any(Report.class))).thenReturn(new ReportResponse());

            reportService.createReport(createRequest);

            verify(reportRepository).save(any(Report.class));
            assertEquals(ReportPriority.HIGH, report.getPriority()); // FRAUD -> HIGH
        }
    }

    @Test
    void getMyReports_ShouldReturnUserReports() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(reporterId));

            when(reportRepository.findByReporterId(eq(reporterId), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(report)));
            when(reportMapper.toResponse(any(Report.class))).thenReturn(new ReportResponse());

            Page<ReportResponse> result = reportService.getMyReports(Pageable.unpaged());

            assertNotNull(result);
            assertEquals(1, result.getSize());
        }
    }

    @Test
    void startInvestigation_ShouldUpdateStatus() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            UUID adminId = UUID.randomUUID();
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(adminId));

            when(reportRepository.findById(report.getId())).thenReturn(Optional.of(report));
            when(userRepository.getReferenceById(adminId)).thenReturn(new User()); // Admin

            reportService.startInvestigation(report.getId());

            assertEquals(ReportStatus.INVESTIGATING, report.getStatus());
            verify(reportRepository).save(report);
        }
    }

    @Test
    void resolveReport_WhenFraudUpheld_ShouldTriggerTransparencyScore() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            UUID adminId = UUID.randomUUID();
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(adminId));

            report.setStatus(ReportStatus.INVESTIGATING);
            when(reportRepository.findById(report.getId())).thenReturn(Optional.of(report));
            when(userRepository.getReferenceById(adminId)).thenReturn(new User());

            ResolveReportRequest resolveRequest = new ResolveReportRequest();
            resolveRequest.setResolution(ReportStatus.RESOLVED);
            resolveRequest.setResolutionNotes("Confirmed fraud");

            reportService.resolveReport(report.getId(), resolveRequest);

            verify(transparencyScoreService).onReportUpheldForOrganization(report.getEntityId(), report.getId());
            assertEquals(ReportStatus.RESOLVED, report.getStatus());
        }
    }

    @Test
    void resolveReport_WhenDismissed_ShouldNotTriggerAction() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            UUID adminId = UUID.randomUUID();
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(adminId));

            report.setStatus(ReportStatus.INVESTIGATING);
            when(reportRepository.findById(report.getId())).thenReturn(Optional.of(report));
            when(userRepository.getReferenceById(adminId)).thenReturn(new User());

            ResolveReportRequest resolveRequest = new ResolveReportRequest();
            resolveRequest.setResolution(ReportStatus.DISMISSED);
            resolveRequest.setResolutionNotes("False alarm");

            reportService.resolveReport(report.getId(), resolveRequest);

            verify(transparencyScoreService, never()).onReportUpheldForOrganization(any(), any());
            assertEquals(ReportStatus.DISMISSED, report.getStatus());
        }
    }
}
