package com.seffafbagis.api.dto.mapper;

import com.seffafbagis.api.dto.request.report.CreateReportRequest;
import com.seffafbagis.api.dto.response.report.ReportDetailResponse;
import com.seffafbagis.api.dto.response.report.ReportResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.report.Report;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.ReportEntityType;
import com.seffafbagis.api.enums.ReportPriority;
import com.seffafbagis.api.enums.ReportStatus;
import com.seffafbagis.api.enums.ReportType;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReportMapper {

    private final CampaignRepository campaignRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public Report toEntity(CreateReportRequest request, User reporter) {
        Report report = new Report();
        report.setReporter(reporter);
        report.setReportType(request.getReportType());
        report.setEntityType(request.getEntityType());
        report.setEntityId(request.getEntityId());
        report.setReason(request.getReason());
        report.setDescription(request.getDescription());
        report.setEvidenceUrls(request.getEvidenceUrls());
        report.setStatus(ReportStatus.PENDING);
        // Priority will be set by service based on type
        return report;
    }

    public ReportResponse toResponse(Report report) {
        ReportResponse response = new ReportResponse();
        mapCommonFields(report, response);
        return response;
    }

    public ReportDetailResponse toDetailResponse(Report report) {
        ReportDetailResponse response = new ReportDetailResponse();
        mapCommonFields(report, response);

        if (report.getReporter() != null) {
            response.setReporterName(report.getReporter().getFullName());
        } else {
            response.setReporterName("Anonymous");
        }

        response.setDescription(report.getDescription());
        response.setEvidenceUrls(report.getEvidenceUrls());

        if (report.getAssignedTo() != null) {
            response.setAssignedToName(report.getAssignedTo().getFullName());
            response.setAssignedAt(report.getAssignedAt());
        }

        if (report.getResolvedBy() != null) {
            response.setResolvedByName(report.getResolvedBy().getFullName());
            response.setResolvedAt(report.getResolvedAt());
            response.setResolutionNotes(report.getResolutionNotes());
        }

        return response;
    }

    private void mapCommonFields(Report report, ReportResponse response) {
        response.setId(report.getId());
        response.setReportType(report.getReportType());
        response.setReportTypeName(getReportTypeName(report.getReportType()));
        response.setEntityType(report.getEntityType());
        response.setEntityTypeName(report.getEntityType().name());
        response.setEntityId(report.getEntityId());
        response.setEntityName(getEntityName(report.getEntityType(), report.getEntityId()));
        response.setReason(report.getReason());
        response.setPriority(report.getPriority());
        response.setPriorityName(getPriorityName(report.getPriority()));
        response.setStatus(report.getStatus());
        response.setStatusName(getStatusName(report.getStatus()));
        response.setCreatedAt(report.getCreatedAt());
        response.setAnonymous(report.getReporter() == null);
    }

    private String getEntityName(ReportEntityType type, UUID entityId) {
        if (type == null || entityId == null)
            return "Unknown";

        return switch (type) {
            case CAMPAIGN -> campaignRepository.findById(entityId)
                    .map(Campaign::getTitle)
                    .orElse("Unknown Campaign");
            case ORGANIZATION -> organizationRepository.findById(entityId)
                    .map(Organization::getLegalName)
                    .orElse("Unknown Organization");
            case USER -> userRepository.findById(entityId)
                    .map(User::getFullName)
                    .orElse("Unknown User");
        };
    }

    private String getReportTypeName(ReportType type) {
        return switch (type) {
            case FRAUD -> "Dolandırıcılık";
            case INAPPROPRIATE -> "Uygunsuz İçerik";
            case SPAM -> "Spam";
            case MISLEADING -> "Yanıltıcı Bilgi";
            case OTHER -> "Diğer";
        };
    }

    private String getStatusName(ReportStatus status) {
        return switch (status) {
            case PENDING -> "Beklemede";
            case IN_REVIEW -> "İncelemede";
            case INVESTIGATING -> "İnceleniyor";
            case RESOLVED -> "Çözüldü";
            case DISMISSED -> "Reddedildi";
            case REJECTED -> "Reddedildi";
            case CLOSED -> "Kapatıldı";
        };
    }

    private String getPriorityName(ReportPriority priority) {
        return switch (priority) {
            case LOW -> "Düşük";
            case MEDIUM -> "Orta";
            case HIGH -> "Yüksek";
            case CRITICAL -> "Kritik";
        };
    }
}
