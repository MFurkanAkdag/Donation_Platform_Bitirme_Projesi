package com.seffafbagis.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {

    private RecurringDonationProperties recurringDonation = new RecurringDonationProperties();
    private BankTransferProperties bankTransfer = new BankTransferProperties();
    private EvidenceReminderProperties evidenceReminder = new EvidenceReminderProperties();
    private CleanupProperties cleanup = new CleanupProperties();

    @Data
    public static class RecurringDonationProperties {
        private boolean enabled = true;
        private int maxRetries = 3;
    }

    @Data
    public static class BankTransferProperties {
        private boolean enabled = true;
        private int expiryHours = 168; // 7 days
    }

    @Data
    public static class EvidenceReminderProperties {
        private boolean enabled = true;
        private List<Integer> reminderDays = List.of(7, 3, 1, 0);
    }

    @Data
    public static class CleanupProperties {
        private boolean enabled = true;
        private int notificationRetentionDays = 90;
        private int auditLogRetentionDays = 365;
    }
}
