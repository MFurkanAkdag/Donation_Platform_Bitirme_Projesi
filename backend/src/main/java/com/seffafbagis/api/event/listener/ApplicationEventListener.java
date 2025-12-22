package com.seffafbagis.api.event.listener;

import com.seffafbagis.api.entity.application.Application;
import com.seffafbagis.api.event.ApplicationStatusChangedEvent;
import com.seffafbagis.api.repository.ApplicationRepository;
import com.seffafbagis.api.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Handles beneficiary application-related events asynchronously.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventListener {

    private final NotificationService notificationService;
    private final ApplicationRepository applicationRepository;

    @Async
    @EventListener
    public void handleApplicationStatusChanged(ApplicationStatusChangedEvent event) {
        log.info("Handling ApplicationStatusChangedEvent for app: {}", event.getApplicationId());

        try {
            Application application = applicationRepository.findById(event.getApplicationId()).orElse(null);
            if (application != null) {
                notificationService.notifyApplicationUpdate(application);
            }

        } catch (Exception e) {
            log.error("Error handling ApplicationStatusChangedEvent for app {}: {}",
                    event.getApplicationId(), e.getMessage(), e);
        }
    }
}
