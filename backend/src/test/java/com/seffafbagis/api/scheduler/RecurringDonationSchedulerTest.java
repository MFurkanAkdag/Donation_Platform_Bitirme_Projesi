package com.seffafbagis.api.scheduler;

import com.seffafbagis.api.config.SchedulerProperties;
import com.seffafbagis.api.entity.donation.RecurringDonation;
import com.seffafbagis.api.repository.RecurringDonationRepository;
import com.seffafbagis.api.service.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringDonationSchedulerTest {

    @Mock
    private RecurringDonationRepository recurringDonationRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private SchedulerProperties schedulerProperties;

    @Mock
    private SchedulerProperties.RecurringDonationProperties recurringDonationProperties;

    @InjectMocks
    private RecurringDonationScheduler scheduler;

    @BeforeEach
    void setUp() {
        lenient().when(schedulerProperties.getRecurringDonation()).thenReturn(recurringDonationProperties);
        lenient().when(recurringDonationProperties.isEnabled()).thenReturn(true);
        lenient().when(recurringDonationProperties.getMaxRetries()).thenReturn(3);
    }

    @Test
    void processRecurringDonations_ShouldProcessEligibleDonations() {
        // Arrange
        RecurringDonation donation = new RecurringDonation();
        donation.setId(java.util.UUID.randomUUID());
        donation.setFrequency("monthly");
        donation.setNextPaymentDate(LocalDate.now());
        donation.setFailureCount(0);

        when(recurringDonationRepository.findByStatusAndNextPaymentDateLessThanEqual("Active", LocalDate.now()))
                .thenReturn(List.of(donation));

        when(paymentService.processRecurringPayment(donation)).thenReturn(true);

        // Act
        scheduler.processRecurringDonations();

        // Assert
        verify(paymentService).processRecurringPayment(donation);
        verify(recurringDonationRepository).save(donation);
    }

    @Test
    void processRecurringDonations_ShouldHandlePaymentFailure() {
        // Arrange
        RecurringDonation donation = new RecurringDonation();
        donation.setId(java.util.UUID.randomUUID());
        donation.setFrequency("monthly");
        donation.setNextPaymentDate(LocalDate.now());
        donation.setFailureCount(0);

        when(recurringDonationRepository.findByStatusAndNextPaymentDateLessThanEqual("Active", LocalDate.now()))
                .thenReturn(List.of(donation));

        when(paymentService.processRecurringPayment(donation)).thenReturn(false);

        // Act
        scheduler.processRecurringDonations();

        // Assert
        verify(paymentService).processRecurringPayment(donation);
        verify(recurringDonationRepository).save(donation);
        assert donation.getFailureCount() == 1;
    }
}
