package com.seffafbagis.api.scheduler;

import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.enums.CampaignStatus;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.service.notification.NotificationService;
import com.seffafbagis.api.service.transparency.TransparencyScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CampaignStatusScheduler {

    private final CampaignRepository campaignRepository;
    private final NotificationService notificationService;
    private final TransparencyScoreService transparencyScoreService;

    @Scheduled(cron = "0 0 */6 * * ?", zone = "Europe/Istanbul")
    public void updateCampaignStatuses() {
        log.info("Starting campaign status updates");

        autoCompleteSuccessfulCampaigns();
        handleExpiredCampaigns();

        log.info("Campaign status updates completed");
    }

    @Transactional
    public void autoCompleteSuccessfulCampaigns() {
        List<Campaign> activeCampaigns = campaignRepository
                .findByStatus(CampaignStatus.ACTIVE, org.springframework.data.domain.Pageable.unpaged()).getContent();

        for (Campaign campaign : activeCampaigns) {
            BigDecimal collected = campaign.getCollectedAmount() != null ? campaign.getCollectedAmount()
                    : BigDecimal.ZERO;
            BigDecimal target = campaign.getTargetAmount() != null ? campaign.getTargetAmount() : BigDecimal.ZERO;

            if (collected.compareTo(target) >= 0) {
                log.info("Auto-completing campaign {} as it reached target", campaign.getId());
                completeCampaign(campaign);
            }
        }
    }

    @Transactional
    public void handleExpiredCampaigns() {
        // Need custom query for finding expired active campaigns or filter in memory
        // findByEndDateBeforeAndStatus
        List<Campaign> expiredCampaigns = campaignRepository.findByEndDateBeforeAndStatus(LocalDateTime.now(),
                CampaignStatus.ACTIVE);

        for (Campaign campaign : expiredCampaigns) {
            BigDecimal collected = campaign.getCollectedAmount() != null ? campaign.getCollectedAmount()
                    : BigDecimal.ZERO;
            BigDecimal target = campaign.getTargetAmount() != null ? campaign.getTargetAmount() : BigDecimal.ZERO;

            BigDecimal eightyPercent = target.multiply(new BigDecimal("0.8"));

            if (collected.compareTo(eightyPercent) >= 0) {
                log.info("Completing expired campaign {} with >80% funds", campaign.getId());
                completeCampaign(campaign);
            } else {
                // Check extensions
                int extensions = campaign.getExtensionCount() != null ? campaign.getExtensionCount() : 0;

                if (extensions < 2) {
                    log.info("Extending campaign {} by 7 days (extension #{})", campaign.getId(), extensions + 1);
                    campaign.setEndDate(campaign.getEndDate().plusDays(7));
                    campaign.setExtensionCount(extensions + 1);
                    campaignRepository.save(campaign);

                    // Notify organization about extension
                    if (campaign.getOrganization() != null && campaign.getOrganization().getUser() != null) {
                        notificationService.createNotification(
                                campaign.getOrganization().getUser().getId(),
                                com.seffafbagis.api.enums.NotificationType.CAMPAIGN_UPDATE,
                                "Kampanya Süresi Uzatıldı",
                                "Kampanyanız hedefe ulaşamadığı için 7 gün uzatıldı. (" + (extensions + 1) + "/2)",
                                java.util.Map.of("campaignId", campaign.getId()));
                    }
                } else {
                    log.info("Completing expired campaign {} after max extensions", campaign.getId());
                    completeCampaign(campaign);
                }
            }
        }
    }

    private void completeCampaign(Campaign campaign) {
        campaign.setStatus(CampaignStatus.COMPLETED);
        campaign.setCompletedAt(LocalDateTime.now());
        campaignRepository.save(campaign);

        notificationService.notifyCampaignCompleted(campaign);
        transparencyScoreService.onCampaignCompleted(campaign.getId());

        // Notify followers - Implementation depends on Follower Repository/Service
        // availability
        // Assuming we rely on generic notification logic or separate job for mass
        // notifications
    }
}
