package com.seffafbagis.api.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for transparency score recalculation.
 */
@Component
@ConditionalOnProperty(name = "scheduler.transparency-score.enabled", havingValue = "true", matchIfMissing = true)
public class TransparencyScoreScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TransparencyScoreScheduler.class);

    @Scheduled(cron = "${scheduler.transparency-score.cron:0 0 2 * * *}")
    public void recalculateScores() {
        logger.info("Starting transparency score recalculation job");
        // Recalculation logic to be implemented
        logger.info("Transparency score recalculation job completed");
    }
}
