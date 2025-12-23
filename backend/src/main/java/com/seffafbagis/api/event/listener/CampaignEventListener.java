package com.seffafbagis.api.event.listener;

import com.seffafbagis.api.event.CampaignCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener for campaign-related events.
 */
@Component
@lombok.RequiredArgsConstructor
public class CampaignEventListener {

    private static final Logger logger = LoggerFactory.getLogger(CampaignEventListener.class);

    private final com.seffafbagis.api.service.notification.NotificationService notificationService;
    private final com.seffafbagis.api.service.transparency.TransparencyScoreService transparencyScoreService;
    private final com.seffafbagis.api.repository.CampaignRepository campaignRepository;
    private final com.seffafbagis.api.service.audit.AuditLogService auditLogService;

    @Async
    @EventListener
    public void handleCampaignCompleted(CampaignCompletedEvent event) {
        logger.info("Campaign completed event received: campaignId={}, organizationId={}",
                event.getCampaignId(), event.getOrganizationId());
        // Additional processing logic can be added here
    }

    @Async
    @EventListener
    public void handleCampaignApproved(com.seffafbagis.api.event.CampaignApprovedEvent event) {
        logger.info("Campaign approved event received: campaignId={}", event.getCampaignId());
        campaignRepository.findById(event.getCampaignId()).ifPresent(campaign -> {
            notificationService.notifyCampaignApproved(campaign);
            auditLogService.log("campaign.approved", event.getTriggeredBy(), "campaign", event.getCampaignId(), null,
                    null);
        });
    }

    @Async
    @EventListener
    public void handleCampaignRejected(com.seffafbagis.api.event.CampaignRejectedEvent event) {
        logger.info("Campaign rejected event received: campaignId={}", event.getCampaignId());
        campaignRepository.findById(event.getCampaignId()).ifPresent(campaign -> {
            notificationService.notifyCampaignRejected(campaign, event.getRejectionReason());
            auditLogService.log("campaign.rejected", event.getTriggeredBy(), "campaign", event.getCampaignId(), null,
                    null);
        });
    }
}
