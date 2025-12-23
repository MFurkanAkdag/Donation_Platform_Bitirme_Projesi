package com.seffafbagis.api.event.listener;

import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.event.DonationCompletedEvent;
import com.seffafbagis.api.event.DonationCreatedEvent;
import com.seffafbagis.api.event.DonationFailedEvent;
import com.seffafbagis.api.repository.DonationRepository;
import com.seffafbagis.api.service.audit.AuditLogService;
import com.seffafbagis.api.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Listener for donation-related events.
 */
@Component
@RequiredArgsConstructor
public class DonationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DonationEventListener.class);

    private final NotificationService notificationService;
    private final DonationRepository donationRepository;
    private final AuditLogService auditLogService;

    @Async
    @EventListener
    public void handleDonationCreated(DonationCreatedEvent event) {
        logger.info("Donation created event received: donationId={}, campaignId={}, amount={}",
                event.getDonationId(), event.getCampaignId(), event.getAmount());
        // Additional processing logic can be added here
    }

    @Async
    @EventListener
    public void handleDonationCompleted(DonationCompletedEvent event) {
        logger.info("Donation completed event received: donationId={}", event.getDonationId());

        Optional<Donation> donationOpt = donationRepository.findById(event.getDonationId());
        if (donationOpt.isPresent()) {
            Donation donation = donationOpt.get();
            notificationService.notifyDonationReceived(donation);
        } else {
            logger.warn("Donation not found for completed event: {}", event.getDonationId());
        }

        auditLogService.log("donation.completed", event.getTriggeredBy(), "donation", event.getDonationId(), null,
                null);
    }

    @Async
    @EventListener
    public void handleDonationFailed(DonationFailedEvent event) {
        logger.info("Donation failed event received: donationId={}, reason={}", event.getDonationId(),
                event.getFailureReason());

        if (event.getDonorId() != null) {
            notificationService.notifySystem(event.getDonorId(), "Bağış Başarısız",
                    "Bağışınız işlenirken bir hata oluştu: " + event.getFailureReason());
        }

        auditLogService.log("donation.failed", event.getTriggeredBy(), "donation", event.getDonationId(), null, null);
    }
}
