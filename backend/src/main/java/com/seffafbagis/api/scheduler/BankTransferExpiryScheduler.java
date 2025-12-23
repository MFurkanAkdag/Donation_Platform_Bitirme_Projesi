package com.seffafbagis.api.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for expiring pending bank transfer references.
 */
@Component
@ConditionalOnProperty(name = "scheduler.bank-transfer.enabled", havingValue = "true", matchIfMissing = true)
public class BankTransferExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BankTransferExpiryScheduler.class);

    @Scheduled(cron = "${scheduler.bank-transfer.cron:0 */15 * * * *}")
    public void expireOldReferences() {
        logger.info("Starting bank transfer expiry job");
        // Expiry logic to be implemented
        logger.info("Bank transfer expiry job completed");
    }
}
