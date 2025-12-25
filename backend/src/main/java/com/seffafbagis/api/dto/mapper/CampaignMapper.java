package com.seffafbagis.api.dto.mapper;

import com.seffafbagis.api.dto.request.campaign.CreateCampaignRequest;
import com.seffafbagis.api.dto.request.campaign.UpdateCampaignRequest;
import com.seffafbagis.api.dto.response.campaign.*;
import com.seffafbagis.api.entity.campaign.*;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CampaignMapper {

    private final CategoryMapper categoryMapper;
    private final OrganizationMapper organizationMapper;

    public CampaignMapper(CategoryMapper categoryMapper, OrganizationMapper organizationMapper) {
        this.categoryMapper = categoryMapper;
        this.organizationMapper = organizationMapper;
    }

    public Campaign toEntity(CreateCampaignRequest request) {
        if (request == null) {
            return null;
        }
        Campaign campaign = new Campaign();
        campaign.setTitle(request.getTitle());
        campaign.setSummary(request.getSummary());
        campaign.setDescription(request.getDescription());
        campaign.setCoverImageUrl(request.getCoverImageUrl());
        campaign.setTargetAmount(request.getTargetAmount());
        campaign.setCurrency(request.getCurrency());
        if (request.getStartDate() != null)
            campaign.setStartDate(request.getStartDate().atStartOfDay());
        if (request.getEndDate() != null)
            campaign.setEndDate(request.getEndDate().atStartOfDay());
        campaign.setEvidenceDeadlineDays(request.getEvidenceDeadlineDays());
        campaign.setIsUrgent(request.getIsUrgent());
        campaign.setLocationCity(request.getLocationCity());
        campaign.setLocationDistrict(request.getLocationDistrict());
        campaign.setBeneficiaryCount(request.getBeneficiaryCount());
        return campaign;
    }

    public void updateEntity(Campaign campaign, UpdateCampaignRequest request) {
        if (request.getTitle() != null)
            campaign.setTitle(request.getTitle());
        if (request.getSummary() != null)
            campaign.setSummary(request.getSummary());
        if (request.getDescription() != null)
            campaign.setDescription(request.getDescription());
        if (request.getCoverImageUrl() != null)
            campaign.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getTargetAmount() != null)
            campaign.setTargetAmount(request.getTargetAmount());
        if (request.getCurrency() != null)
            campaign.setCurrency(request.getCurrency());
        if (request.getStartDate() != null)
            campaign.setStartDate(request.getStartDate().atStartOfDay());
        if (request.getEndDate() != null)
            campaign.setEndDate(request.getEndDate().atStartOfDay());
        if (request.getEvidenceDeadlineDays() != null)
            campaign.setEvidenceDeadlineDays(request.getEvidenceDeadlineDays());
        if (request.getIsUrgent() != null)
            campaign.setIsUrgent(request.getIsUrgent());
        if (request.getLocationCity() != null)
            campaign.setLocationCity(request.getLocationCity());
        if (request.getLocationDistrict() != null)
            campaign.setLocationDistrict(request.getLocationDistrict());
        if (request.getBeneficiaryCount() != null)
            campaign.setBeneficiaryCount(request.getBeneficiaryCount());
    }

    public CampaignResponse toResponse(Campaign entity) {
        if (entity == null)
            return null;

        CampaignResponse response = new CampaignResponse();
        mapCommonFields(entity, response);
        return response;
    }

    public CampaignListResponse toListResponse(Campaign entity) {
        if (entity == null)
            return null;

        return CampaignListResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .slug(entity.getSlug())
                .coverImageUrl(entity.getCoverImageUrl())
                .targetAmount(entity.getTargetAmount())
                .collectedAmount(entity.getCollectedAmount())
                .progressPercentage(calculateProgress(entity.getCollectedAmount(), entity.getTargetAmount()))
                .donorCount(entity.getDonorCount())
                .isUrgent(entity.getIsUrgent())
                .isFeatured(entity.getIsFeatured())
                .organizationName(entity.getOrganization() != null ? entity.getOrganization().getLegalName() : null)
                .organizationLogo(entity.getOrganization() != null ? entity.getOrganization().getLogoUrl() : null)
                .daysRemaining(calculateDaysRemaining(entity.getEndDate()))
                .build();
    }

    public CampaignDetailResponse toDetailResponse(Campaign entity) {
        if (entity == null)
            return null;

        CampaignDetailResponse response = new CampaignDetailResponse();
        mapCommonFields(entity, response);

        response.setDescription(entity.getDescription());
        response.setBeneficiaryCount(entity.getBeneficiaryCount());
        response.setEvidenceDeadlineDays(entity.getEvidenceDeadlineDays());
        response.setApprovedAt(entity.getApprovedAt());
        response.setCompletedAt(entity.getCompletedAt());

        if (entity.getOrganization() != null) {
            response.setOrganization(organizationMapper.toSummaryResponse(entity.getOrganization()));
        }

        if (entity.getCategories() != null) {
            response.setCategories(entity.getCategories().stream()
                    .map(cc -> categoryMapper.toResponse(cc.getCategory()))
                    .collect(Collectors.toList()));
        }

        if (entity.getDonationTypes() != null) {
            response.setDonationTypes(entity.getDonationTypes().stream()
                    .map(cdt -> categoryMapper.toResponse(cdt.getDonationType()))
                    .collect(Collectors.toList()));
        }

        return response;
    }

    public CampaignSummaryResponse toSummaryResponse(Campaign entity) {
        if (entity == null)
            return null;

        return CampaignSummaryResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .slug(entity.getSlug())
                .coverImageUrl(entity.getCoverImageUrl())
                .targetAmount(entity.getTargetAmount())
                .collectedAmount(entity.getCollectedAmount())
                .build();
    }

    public CampaignUpdateResponse toResponse(CampaignUpdate entity) {
        if (entity == null)
            return null;

        return CampaignUpdateResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .imageUrl(entity.getImageUrl())
                .createdByName(entity.getCreatedBy() != null
                        ? entity.getCreatedBy().getFullName()
                        : null)
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDateTime() : null)
                .build();
    }

    public CampaignImageResponse toResponse(CampaignImage entity) {
        if (entity == null)
            return null;

        return CampaignImageResponse.builder()
                .id(entity.getId())
                .imageUrl(entity.getImageUrl())
                .thumbnailUrl(entity.getThumbnailUrl())
                .caption(entity.getCaption())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }

    private void mapCommonFields(Campaign entity, CampaignResponse response) {
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setSlug(entity.getSlug());
        response.setSummary(entity.getSummary());
        response.setCoverImageUrl(entity.getCoverImageUrl());
        response.setTargetAmount(entity.getTargetAmount());
        response.setCollectedAmount(entity.getCollectedAmount());
        response.setDonorCount(entity.getDonorCount());
        response.setCurrency(entity.getCurrency());
        response.setStatus(entity.getStatus().name());
        response.setStartDate(entity.getStartDate());
        response.setEndDate(entity.getEndDate());
        response.setIsUrgent(entity.getIsUrgent());
        response.setIsFeatured(entity.getIsFeatured());
        response.setLocationCity(entity.getLocationCity());
        response.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDateTime() : null);

        if (entity.getOrganization() != null) {
            response.setOrganizationId(entity.getOrganization().getId());
            response.setOrganizationName(entity.getOrganization().getLegalName());
            response.setOrganizationLogo(entity.getOrganization().getLogoUrl());
        }

        response.setProgressPercentage(calculateProgress(entity.getCollectedAmount(), entity.getTargetAmount()));

        response.setRealizationStatus(entity.getRealizationStatus());
        response.setRealizationDeadline(entity.getRealizationDeadline());
    }

    private BigDecimal calculateProgress(BigDecimal collected, BigDecimal target) {
        if (target == null || target.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        if (collected == null)
            collected = BigDecimal.ZERO;

        return collected.divide(target, 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    private Long calculateDaysRemaining(LocalDateTime endDate) {
        if (endDate == null)
            return null;
        return ChronoUnit.DAYS.between(LocalDateTime.now(), endDate);
    }
}
