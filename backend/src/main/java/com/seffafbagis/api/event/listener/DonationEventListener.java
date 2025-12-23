package com.seffafbagis.api.event.listener;

import com.seffafbagis.api.event.DonationCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener for donation-related events.
 */
@Component
public class DonationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DonationEventListener.class);

    @Async
    @EventListener
    public void handleDonationCreated(DonationCreatedEvent event) {
        logger.info("Donation created event received: donationId={}, campaignId={}, amount={}",
                event.getDonationId(), event.getCampaignId(), event.getAmount());
        // Additional processing logic can be added here
    }
}
