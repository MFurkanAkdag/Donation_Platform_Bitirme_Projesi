package com.seffafbagis.api.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for sending evidence upload reminders.
 */
@Component
@ConditionalOnProperty(name = "scheduler.evidence-reminder.enabled", havingValue = "true", matchIfMissing = true)
public class EvidenceReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(EvidenceReminderScheduler.class);

    @Scheduled(cron = "${scheduler.evidence-reminder.cron:0 0 9 * * *}")
    public void sendReminders() {
        logger.info("Starting evidence reminder job");
        // Reminder logic to be implemented
        logger.info("Evidence reminder job completed");
    }
}
