package com.seffafbagis.api.event.listener;

import com.seffafbagis.api.event.EvidenceApprovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener for evidence-related events.
 */
@Component
public class EvidenceEventListener {

    private static final Logger logger = LoggerFactory.getLogger(EvidenceEventListener.class);

    @Async
    @EventListener
    public void handleEvidenceApproved(EvidenceApprovedEvent event) {
        logger.info("Evidence approved event received: evidenceId={}, campaignId={}, onTime={}",
                event.getEvidenceId(), event.getCampaignId(), event.getOnTime());
        // Additional processing logic can be added here
    }
}
