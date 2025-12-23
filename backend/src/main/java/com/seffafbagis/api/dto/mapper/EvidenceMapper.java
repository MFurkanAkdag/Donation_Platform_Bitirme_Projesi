package com.seffafbagis.api.dto.mapper;

import com.seffafbagis.api.dto.request.evidence.CreateEvidenceRequest;
import com.seffafbagis.api.dto.response.evidence.CampaignEvidenceSummaryResponse;
import com.seffafbagis.api.dto.response.evidence.EvidenceDetailResponse;
import com.seffafbagis.api.dto.response.evidence.EvidenceDocumentResponse;
import com.seffafbagis.api.dto.response.evidence.EvidenceResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.evidence.Evidence;
import com.seffafbagis.api.entity.evidence.EvidenceDocument;
import com.seffafbagis.api.entity.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EvidenceMapper {

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "campaign", source = "campaign")
        @Mapping(target = "uploadedBy", source = "uploadedBy")
        @Mapping(target = "documents", ignore = true) // Handled manually in service
        @Mapping(target = "status", ignore = true) // Set default in entity
        @Mapping(target = "reviewedBy", ignore = true)
        @Mapping(target = "reviewedAt", ignore = true)
        @Mapping(target = "rejectionReason", ignore = true)
        @Mapping(target = "uploadedAt", ignore = true) // Set default in entity
        @Mapping(target = "title", source = "request.title")
        @Mapping(target = "description", source = "request.description")
        @Mapping(target = "amountSpent", source = "request.amountSpent")
        @Mapping(target = "spendDate", source = "request.spendDate")
        @Mapping(target = "vendorName", source = "request.vendorName")
        @Mapping(target = "vendorTaxNumber", source = "request.vendorTaxNumber")
        @Mapping(target = "invoiceNumber", source = "request.invoiceNumber")
        @Mapping(target = "evidenceType", source = "request.evidenceType")
        Evidence toEntity(CreateEvidenceRequest request, Campaign campaign, User uploadedBy);

        @Mapping(target = "campaignId", source = "campaign.id")
        @Mapping(target = "campaignTitle", source = "campaign.title")
        @Mapping(target = "uploadedByName", source = "uploadedBy.fullName")
        @Mapping(target = "reviewedBy", ignore = true)
        EvidenceResponse toResponse(Evidence evidence);

        @Mapping(target = "campaignId", source = "campaign.id")
        @Mapping(target = "campaignTitle", source = "campaign.title")
        @Mapping(target = "uploadedByName", source = "uploadedBy.fullName")
        @Mapping(target = "reviewedByName", source = "reviewedBy.fullName")
        @Mapping(target = "reviewedBy", ignore = true)
        EvidenceDetailResponse toDetailResponse(Evidence evidence);

        EvidenceDocumentResponse toDocumentResponse(EvidenceDocument document);

        default CampaignEvidenceSummaryResponse toSummaryResponse(Campaign campaign, long totalEvidences,
                        long approvedCount, long pendingCount, BigDecimal totalAmountDocumented) {
                LocalDateTime deadline = campaign.getCompletedAt() != null
                                ? campaign.getCompletedAt()
                                                .plusDays(campaign.getEvidenceDeadlineDays() != null
                                                                ? campaign.getEvidenceDeadlineDays()
                                                                : 15)
                                : null;

                long daysRemaining = 0;
                boolean isOverdue = false;

                if (deadline != null) {
                        daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), deadline);
                        isOverdue = LocalDateTime.now().isAfter(deadline);
                }

                return CampaignEvidenceSummaryResponse.builder()
                                .totalEvidences(totalEvidences)
                                .approvedCount(approvedCount)
                                .pendingCount(pendingCount)
                                .totalAmountDocumented(
                                                totalAmountDocumented != null ? totalAmountDocumented : BigDecimal.ZERO)
                                .deadline(deadline)
                                .daysRemaining(daysRemaining)
                                .isOverdue(isOverdue)
                                .build();
        }
}
