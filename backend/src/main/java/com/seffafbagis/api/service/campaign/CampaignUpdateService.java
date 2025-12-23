package com.seffafbagis.api.service.campaign;

import com.seffafbagis.api.dto.mapper.CampaignMapper;
import com.seffafbagis.api.dto.request.campaign.AddCampaignUpdateRequest;
import com.seffafbagis.api.dto.response.campaign.CampaignUpdateResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.campaign.CampaignUpdate;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.exception.ForbiddenException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.CampaignUpdateRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CampaignUpdateService {

    private final CampaignUpdateRepository campaignUpdateRepository;
    private final CampaignRepository campaignRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final CampaignMapper campaignMapper;

    @Transactional(readOnly = true)
    public Page<CampaignUpdateResponse> getUpdates(UUID campaignId, Pageable pageable) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new ResourceNotFoundException("Campaign not found");
        }
        return campaignUpdateRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId, pageable)
                .map(campaignMapper::toResponse);
    }

    public CampaignUpdateResponse addUpdate(UUID campaignId, AddCampaignUpdateRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));

        verifyOwner(campaign);

        CampaignUpdate update = new CampaignUpdate();
        update.setCampaign(campaign);
        update.setTitle(request.getTitle());
        update.setContent(request.getContent());
        update.setImageUrl(request.getImageUrl());
        update.setCreatedBy(userRepository.getReferenceById(SecurityUtils.getCurrentUserId().orElseThrow()));

        return campaignMapper.toResponse(campaignUpdateRepository.save(update));
    }

    public void deleteUpdate(UUID updateId) {
        CampaignUpdate update = campaignUpdateRepository.findById(updateId)
                .orElseThrow(() -> new ResourceNotFoundException("Update not found"));

        verifyOwner(update.getCampaign());

        campaignUpdateRepository.delete(update);
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
