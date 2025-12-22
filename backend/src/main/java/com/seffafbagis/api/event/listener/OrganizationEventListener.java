package com.seffafbagis.api.event.listener;

import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.event.OrganizationRejectedEvent;
import com.seffafbagis.api.event.OrganizationVerifiedEvent;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.service.notification.EmailService;
import com.seffafbagis.api.service.notification.NotificationService;
import com.seffafbagis.api.service.transparency.TransparencyScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Handles organization-related events asynchronously.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrganizationEventListener {

    private final TransparencyScoreService transparencyScoreService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final OrganizationRepository organizationRepository;

    @Async
    @EventListener
    public void handleOrganizationVerified(OrganizationVerifiedEvent event) {
        log.info("Handling OrganizationVerifiedEvent for org: {}", event.getOrganizationId());

        try {
            // Initialize transparency score
            transparencyScoreService.initializeScore(event.getOrganizationId());

            // Notify user
            notificationService.notifySystem(event.getUserId(),
                    "Kuruluş Onaylandı",
                    "Tebrikler! Kuruluşunuz onaylandı ve artık kampanya oluşturabilirsiniz.");

            // Send verification success email
            Organization org = organizationRepository.findById(event.getOrganizationId()).orElse(null);
            if (org != null) {
                emailService.sendVerificationSuccessEmail(org);
            }

        } catch (Exception e) {
            log.error("Error handling OrganizationVerifiedEvent for org {}: {}",
                    event.getOrganizationId(), e.getMessage(), e);
        }
    }

    @Async
    @EventListener
    public void handleOrganizationRejected(OrganizationRejectedEvent event) {
        log.info("Handling OrganizationRejectedEvent for org: {}", event.getOrganizationId());

        try {
            // Notify user with rejection reason
            if (event.getTriggeredBy() != null) {
                // Get the organization owner user ID
                Organization org = organizationRepository.findById(event.getOrganizationId()).orElse(null);
                if (org != null && org.getUser() != null) {
                    notificationService.notifySystem(org.getUser().getId(),
                            "Kuruluş Başvurusu Reddedildi",
                            "Başvurunuz reddedildi. Sebep: " + event.getRejectionReason());
                }
            }

        } catch (Exception e) {
            log.error("Error handling OrganizationRejectedEvent for org {}: {}",
                    event.getOrganizationId(), e.getMessage(), e);
        }
    }
}
