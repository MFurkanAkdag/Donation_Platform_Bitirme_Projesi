package com.seffafbagis.api.service.campaign;

import com.seffafbagis.api.dto.mapper.CampaignMapper;
import com.seffafbagis.api.dto.request.campaign.AddCampaignImageRequest;
import com.seffafbagis.api.dto.response.campaign.CampaignImageResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.campaign.CampaignImage;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.exception.ForbiddenException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.CampaignImageRepository;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CampaignImageService {

    private final CampaignImageRepository campaignImageRepository;
    private final CampaignRepository campaignRepository;
    private final OrganizationRepository organizationRepository;
    private final CampaignMapper campaignMapper;

    @Transactional(readOnly = true)
    public List<CampaignImageResponse> getImages(UUID campaignId) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new ResourceNotFoundException("Campaign not found");
        }
        return campaignImageRepository.findByCampaignIdOrderByDisplayOrderAsc(campaignId)
                .stream()
                .map(campaignMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CampaignImageResponse addImage(UUID campaignId, AddCampaignImageRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));

        verifyOwner(campaign);

        CampaignImage image = new CampaignImage();
        image.setCampaign(campaign);
        image.setImageUrl(request.getImageUrl());
        image.setThumbnailUrl(request.getThumbnailUrl());
        image.setCaption(request.getCaption());
        image.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);

        return campaignMapper.toResponse(campaignImageRepository.save(image));
    }

    public void deleteImage(UUID imageId) {
        CampaignImage image = campaignImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        verifyOwner(image.getCampaign());

        campaignImageRepository.delete(image);
    }

    public void reorderImages(UUID campaignId, List<UUID> orderedImageIds) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        verifyOwner(campaign);

        List<CampaignImage> images = campaignImageRepository.findByCampaignIdOrderByDisplayOrderAsc(campaignId);

        for (int i = 0; i < orderedImageIds.size(); i++) {
            UUID imageId = orderedImageIds.get(i);
            CampaignImage image = images.stream()
                    .filter(img -> img.getId().equals(imageId))
                    .findFirst()
                    .orElse(null);

            if (image != null) {
                image.setDisplayOrder(i);
                campaignImageRepository.save(image);
            }
        }
    }

    private void verifyOwner(Campaign campaign) {
        UUID currentUserId = SecurityUtils.getCurrentUserId().orElseThrow();
        Organization userOrg = organizationRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ForbiddenException("User has no organization"));

        if (!campaign.getOrganization().getId().equals(userOrg.getId())) {
            throw new ForbiddenException("You do not have permission");
        }
    }
}
