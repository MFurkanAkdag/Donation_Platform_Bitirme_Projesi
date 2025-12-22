package com.seffafbagis.api.dto.mapper;

import com.seffafbagis.api.dto.request.application.CreateApplicationRequest;
import com.seffafbagis.api.dto.response.application.ApplicationDetailResponse;
import com.seffafbagis.api.dto.response.application.ApplicationDocumentResponse;
import com.seffafbagis.api.dto.response.application.ApplicationResponse;
import com.seffafbagis.api.entity.category.Category;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.application.Application;
import com.seffafbagis.api.entity.application.ApplicationDocument;
import com.seffafbagis.api.enums.ApplicationStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class ApplicationMapper {

    public Application toEntity(CreateApplicationRequest request, User applicant, Category category) {
        Application application = new Application();
        application.setApplicant(applicant);
        application.setCategory(category);
        application.setTitle(request.getTitle());
        application.setDescription(request.getDescription());
        application.setRequestedAmount(request.getRequestedAmount());
        application.setStatus(ApplicationStatus.PENDING);
        application.setLocationCity(request.getLocationCity());
        application.setLocationDistrict(request.getLocationDistrict());
        application.setHouseholdSize(request.getHouseholdSize());
        application.setUrgencyLevel(request.getUrgencyLevel() != null ? request.getUrgencyLevel() : 1);
        return application;
    }

    public ApplicationResponse toResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setApplicantId(application.getApplicant().getId());
        if (application.getCategory() != null) {
            response.setCategoryId(application.getCategory().getId());
            response.setCategoryName(application.getCategory().getName());
        }
        response.setTitle(application.getTitle());
        response.setDescription(application.getDescription());
        response.setRequestedAmount(application.getRequestedAmount());
        response.setStatus(application.getStatus());
        response.setLocationCity(application.getLocationCity());
        response.setLocationDistrict(application.getLocationDistrict());
        response.setHouseholdSize(application.getHouseholdSize());
        response.setUrgencyLevel(application.getUrgencyLevel());
        response.setCreatedAt(application.getCreatedAt()); // Assuming BaseEntity has getCreatedAt()
        // If BaseEntity does not rely on Lombok @Getter, needed getters might be
        // missing or different.
        // I will assume standard BaseEntity behavior.
        return response;
    }

    public ApplicationDetailResponse toDetailResponse(Application application) {
        ApplicationDetailResponse response = new ApplicationDetailResponse();
        // Map common fields manually or via conversion
        response.setId(application.getId());
        response.setApplicantId(application.getApplicant().getId());
        if (application.getCategory() != null) {
            response.setCategoryId(application.getCategory().getId());
            response.setCategoryName(application.getCategory().getName());
        }
        response.setTitle(application.getTitle());
        response.setDescription(application.getDescription());
        response.setRequestedAmount(application.getRequestedAmount());
        response.setStatus(application.getStatus());
        response.setLocationCity(application.getLocationCity());
        response.setLocationDistrict(application.getLocationDistrict());
        response.setHouseholdSize(application.getHouseholdSize());
        response.setUrgencyLevel(application.getUrgencyLevel());
        response.setCreatedAt(application.getCreatedAt());

        // Detail specific
        if (application.getAssignedOrganization() != null) {
            response.setAssignedOrganizationId(application.getAssignedOrganization().getId());
            response.setAssignedOrganizationName(application.getAssignedOrganization().getLegalName());
        }

        if (application.getAssignedCampaign() != null) {
            response.setAssignedCampaignId(application.getAssignedCampaign().getId());
            response.setAssignedCampaignTitle(application.getAssignedCampaign().getTitle());
        }

        if (application.getReviewedBy() != null) {
            response.setReviewedByName(application.getReviewedBy().getFullName());
        }
        response.setReviewedAt(application.getReviewedAt());

        // Documents
        if (application.getDocuments() != null) {
            response.setDocuments(application.getDocuments().stream()
                    .map(this::toDocumentResponse)
                    .collect(Collectors.toList()));
        } else {
            response.setDocuments(Collections.emptyList());
        }

        return response;
    }

    public ApplicationDocumentResponse toDocumentResponse(ApplicationDocument document) {
        ApplicationDocumentResponse response = new ApplicationDocumentResponse();
        response.setId(document.getId());
        response.setDocumentType(document.getDocumentType());
        // Simple mapping for display name, could be improved with an Enum lookup or
        // properties
        response.setDocumentTypeName(getDocumentTypeName(document.getDocumentType()));
        response.setFileName(document.getFileName());
        response.setFileUrl(document.getFileUrl());
        response.setIsVerified(document.getIsVerified());
        response.setUploadedAt(document.getUploadedAt());
        return response;
    }

    private String getDocumentTypeName(String type) {
        // Can be improved later
        return switch (type) {
            case "id_card" -> "Kimlik Fotokopisi";
            case "income_proof" -> "Gelir Belgesi";
            case "medical_report" -> "Sağlık Raporu";
            case "utility_bill" -> "Fatura (İkamet)";
            case "disability_card" -> "Engelli Kartı";
            case "student_certificate" -> "Öğrenci Belgesi";
            case "other" -> "Diğer";
            default -> type;
        };
    }
}
