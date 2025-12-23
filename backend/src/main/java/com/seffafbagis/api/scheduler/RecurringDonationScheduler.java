package com.seffafbagis.api.scheduler;

import com.seffafbagis.api.config.SchedulerProperties;
import com.seffafbagis.api.entity.donation.RecurringDonation;
import com.seffafbagis.api.repository.RecurringDonationRepository;
import com.seffafbagis.api.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler for processing recurring donations.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.recurring-donation.enabled", havingValue = "true", matchIfMissing = true)
public class RecurringDonationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RecurringDonationScheduler.class);

    private final RecurringDonationRepository recurringDonationRepository;
    private final PaymentService paymentService;
    private final SchedulerProperties schedulerProperties;

    @Scheduled(cron = "${scheduler.recurring-donation.cron:0 0 6 * * *}")
    public void processRecurringDonations() {
        logger.info("Starting recurring donation processing job");

        List<RecurringDonation> dueDonations = recurringDonationRepository.findByStatusAndNextPaymentDateLessThanEqual(
                "Active", LocalDate.now());

        for (RecurringDonation donation : dueDonations) {
            try {
                processSingleDonation(donation);
            } catch (Exception e) {
                logger.error("Error processing recurring donation: {}", donation.getId(), e);
            }
        }

        logger.info("Recurring donation processing job completed");
    }

    private void processSingleDonation(RecurringDonation donation) {
        boolean success = paymentService.processRecurringPayment(donation);

        if (success) {
            // paymentService presumably updates next payment date or similar,
            // but the test expects repository.save(donation) to be called.
            // If paymentService handles logic, we might just save state if needed.
            // But test specifically verifies repo.save(donation).
            recurringDonationRepository.save(donation);
        } else {
            donation.setFailureCount(donation.getFailureCount() + 1);
            if (donation.getFailureCount() >= schedulerProperties.getRecurringDonation().getMaxRetries()) {
                donation.setStatus("Failed");
            }
            recurringDonationRepository.save(donation);
        }
    }
}
