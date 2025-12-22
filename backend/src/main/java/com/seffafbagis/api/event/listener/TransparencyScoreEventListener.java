package com.seffafbagis.api.event.listener;

import com.seffafbagis.api.event.TransparencyScoreChangedEvent;
import com.seffafbagis.api.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Handles transparency score change events asynchronously.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransparencyScoreEventListener {

    private final NotificationService notificationService;

    private static final BigDecimal CAMPAIGN_CREATION_THRESHOLD = new BigDecimal("40");

    @Async
    @EventListener
    public void handleScoreChanged(TransparencyScoreChangedEvent event) {
        log.info("Handling TransparencyScoreChangedEvent for org: {}", event.getOrganizationId());

        try {
            // Notify organization about score change
            notificationService.notifyScoreChange(event.getOrganizationId(),
                    event.getPreviousScore(), event.getNewScore());

            // Check if score dropped below threshold
            boolean previousAboveThreshold = event.getPreviousScore() != null
                    && event.getPreviousScore().compareTo(CAMPAIGN_CREATION_THRESHOLD) >= 0;
            boolean newBelowThreshold = event.getNewScore().compareTo(CAMPAIGN_CREATION_THRESHOLD) < 0;

            if (previousAboveThreshold && newBelowThreshold) {
                // Score dropped below campaign creation threshold
                log.warn("Organization {} score dropped below threshold", event.getOrganizationId());

                if (event.getTriggeredBy() != null) {
                    notificationService.notifySystem(event.getTriggeredBy(),
                            "Şeffaflık Skoru Uyarısı",
                            "Şeffaflık skorunuz " + CAMPAIGN_CREATION_THRESHOLD + " altına düştü. " +
                                    "Yeni kampanya oluşturma yetkiniz geçici olarak askıya alındı.");
                }
            }

        } catch (Exception e) {
            log.error("Error handling TransparencyScoreChangedEvent for org {}: {}",
                    event.getOrganizationId(), e.getMessage(), e);
        }
    }
}
