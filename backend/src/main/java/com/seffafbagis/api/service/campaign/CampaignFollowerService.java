package com.seffafbagis.api.service.campaign;

import com.seffafbagis.api.dto.mapper.CampaignMapper;
import com.seffafbagis.api.dto.response.campaign.CampaignResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.campaign.CampaignFollower;
import com.seffafbagis.api.entity.campaign.CampaignFollowerId;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.CampaignFollowerRepository;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CampaignFollowerService {

    private final CampaignFollowerRepository campaignFollowerRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final CampaignMapper campaignMapper;

    public void followCampaign(UUID campaignId) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow();
        if (!campaignRepository.existsById(campaignId)) {
            throw new ResourceNotFoundException("Campaign not found");
        }

        CampaignFollowerId id = new CampaignFollowerId(userId, campaignId);
        if (campaignFollowerRepository.existsByCampaignIdAndUserId(campaignId, userId)) {
            return; // Already following
        }

        CampaignFollower follower = CampaignFollower.builder()
                .id(id)
                .user(userRepository.getReferenceById(userId))
                .campaign(campaignRepository.getReferenceById(campaignId))
                .notifyOnUpdate(true)
                .notifyOnComplete(true)
                .build();

        campaignFollowerRepository.save(follower);
    }

    public void unfollowCampaign(UUID campaignId) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow();
        campaignFollowerRepository.deleteByCampaignIdAndUserId(campaignId, userId);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(UUID campaignId) {
        return SecurityUtils.getCurrentUserId()
                .map(userId -> campaignFollowerRepository.existsByCampaignIdAndUserId(campaignId, userId))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponse> getFollowedCampaigns(Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow();
        List<CampaignFollower> followers = campaignFollowerRepository.findByUserId(userId);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), followers.size());
        List<CampaignFollower> pageContent = (start <= end && start < followers.size())
                ? followers.subList(start, end)
                : List.of();

        List<CampaignResponse> responses = pageContent.stream()
                .map(follower -> campaignMapper.toResponse(follower.getCampaign()))
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, followers.size());
    }

    public List<UUID> getFollowersToNotify(UUID campaignId, boolean forUpdate) {
        // Assuming custom method in repository or filter
        List<CampaignFollower> followers = campaignFollowerRepository.findByCampaignId(campaignId);
        return followers.stream()
                .filter(f -> forUpdate ? f.getNotifyOnUpdate() : f.getNotifyOnComplete())
                .map(f -> f.getUser().getId())
                .collect(Collectors.toList());
    }
}
