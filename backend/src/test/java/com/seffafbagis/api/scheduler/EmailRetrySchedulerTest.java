package com.seffafbagis.api.scheduler;

import com.seffafbagis.api.entity.notification.EmailLog;
import com.seffafbagis.api.repository.EmailLogRepository;
import com.seffafbagis.api.service.notification.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailRetrySchedulerTest {

    @Mock
    private EmailLogRepository emailLogRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailRetryScheduler scheduler;

    @Test
    void retryFailedEmails_ShouldProcessFailedLogs() {
        EmailLog logEntry = new EmailLog();
        logEntry.setId(UUID.randomUUID());
        logEntry.setRetryCount(0);
        logEntry.setStatus("failed");

        when(emailLogRepository.findByStatusAndRetryCountLessThan(eq("failed"), eq(5), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(logEntry)));

        scheduler.retryFailedEmails();

        // Verify that we attempt to update it (since we can't fully resend in current
        // impl)
        verify(emailLogRepository).save(logEntry);
        assert logEntry.getRetryCount() == 1;
    }

    @Test
    void retryFailedEmails_ShouldHandleEmptyList() {
        when(emailLogRepository.findByStatusAndRetryCountLessThan(any(), anyInt(), any()))
                .thenReturn(Page.empty());

        scheduler.retryFailedEmails();

        verify(emailLogRepository, never()).save(any());
    }
}
