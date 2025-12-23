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
public class CampaignEventListener {

    private static final Logger logger = LoggerFactory.getLogger(CampaignEventListener.class);

    @Async
    @EventListener
    public void handleCampaignCompleted(CampaignCompletedEvent event) {
        logger.info("Campaign completed event received: campaignId={}, organizationId={}",
                event.getCampaignId(), event.getOrganizationId());
        // Additional processing logic can be added here
    }
}
