package com.seffafbagis.api.scheduler;

import com.seffafbagis.api.config.SchedulerProperties;
import com.seffafbagis.api.entity.donation.BankTransferReference;
import com.seffafbagis.api.repository.BankTransferReferenceRepository;
import com.seffafbagis.api.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import com.seffafbagis.api.enums.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Scheduler for expiring pending bank transfer references.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.bank-transfer.enabled", havingValue = "true", matchIfMissing = true)
public class BankTransferExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BankTransferExpiryScheduler.class);

    private final BankTransferReferenceRepository bankTransferReferenceRepository;
    private final NotificationService notificationService;
    private final SchedulerProperties schedulerProperties;

    @Scheduled(cron = "${scheduler.bank-transfer.cron:0 */15 * * * *}")
    public void expireBankTransfers() {
        logger.info("Starting bank transfer expiry job");

        // This query matches what the test mocks
        List<BankTransferReference> expiredRefs = bankTransferReferenceRepository.findByStatusAndExpiresAtBefore(
                "Pending", OffsetDateTime.now());

        for (BankTransferReference ref : expiredRefs) {
            expireReference(ref);
        }

        logger.info("Bank transfer expiry job completed");
    }

    private void expireReference(BankTransferReference ref) {
        ref.setStatus("Expired");
        bankTransferReferenceRepository.save(ref);

        if (ref.getDonor() != null) {
            notificationService.createNotification(
                    ref.getDonor().getId(),
                    NotificationType.BANK_TRANSFER_EXPIRED,
                    "Bank Transfer Expired",
                    "Your bank transfer request has expired.",
                    null);
        }
    }
}
