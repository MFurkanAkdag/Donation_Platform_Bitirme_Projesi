package com.seffafbagis.api.scheduler;

import com.seffafbagis.api.config.SchedulerProperties;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.enums.CampaignStatus;
import com.seffafbagis.api.enums.EvidenceStatus;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.EvidenceRepository;
import com.seffafbagis.api.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduler for sending evidence upload reminders.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.evidence-reminder.enabled", havingValue = "true", matchIfMissing = true)
public class EvidenceReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(EvidenceReminderScheduler.class);

    private final CampaignRepository campaignRepository;
    private final EvidenceRepository evidenceRepository;
    private final NotificationService notificationService;
    private final SchedulerProperties schedulerProperties;

    @Scheduled(cron = "${scheduler.evidence-reminder.cron:0 0 9 * * *}")
    public void sendEvidenceReminders() {
        logger.info("Starting evidence reminder job");

        int page = 0;
        int size = 50;
        Page<Campaign> campaignPage;

        do {
            campaignPage = campaignRepository.findByStatus(CampaignStatus.COMPLETED, PageRequest.of(page, size));
            for (Campaign campaign : campaignPage.getContent()) {
                checkEvidenceDeadlines(campaign);
            }
            page++;
        } while (campaignPage.hasNext());

        logger.info("Evidence reminder job completed");
    }

    private void checkEvidenceDeadlines(Campaign campaign) {
        if (campaign.getCompletedAt() == null) {
            return;
        }

        int deadlineDays = campaign.getEvidenceDeadlineDays() != null ? campaign.getEvidenceDeadlineDays() : 30; // Default
                                                                                                                 // 30
                                                                                                                 // as
                                                                                                                 // per
                                                                                                                 // test
                                                                                                                 // implication/common
                                                                                                                 // sense
        LocalDateTime deadline = campaign.getCompletedAt().plusDays(deadlineDays);
        long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), deadline);

        List<Integer> reminderDays = schedulerProperties.getEvidenceReminder().getReminderDays();

        if (reminderDays.contains((int) daysRemaining)) {
            BigDecimal totalSpent = evidenceRepository.sumAmountSpentByCampaignIdAndStatus(
                    campaign.getId(), EvidenceStatus.APPROVED);

            if (totalSpent == null) {
                totalSpent = BigDecimal.ZERO;
            }

            BigDecimal collectedAmount = campaign.getCollectedAmount() != null ? campaign.getCollectedAmount()
                    : BigDecimal.ZERO;

            if (totalSpent.compareTo(collectedAmount) < 0) {
                notificationService.notifyEvidenceRequired(campaign, (int) daysRemaining);
            }
        }
    }
}
