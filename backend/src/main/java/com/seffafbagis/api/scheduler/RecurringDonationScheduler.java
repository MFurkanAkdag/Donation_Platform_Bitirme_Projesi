package com.seffafbagis.api.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for processing recurring donations.
 */
@Component
@ConditionalOnProperty(name = "scheduler.recurring-donation.enabled", havingValue = "true", matchIfMissing = true)
public class RecurringDonationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RecurringDonationScheduler.class);

    @Scheduled(cron = "${scheduler.recurring-donation.cron:0 0 6 * * *}")
    public void processRecurringDonations() {
        logger.info("Starting recurring donation processing job");
        // Processing logic to be implemented
        logger.info("Recurring donation processing job completed");
    }
}
