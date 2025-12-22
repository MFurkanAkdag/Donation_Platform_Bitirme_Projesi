package com.seffafbagis.api.service.donation;

import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.donation.DonationReceipt;
import com.seffafbagis.api.repository.DonationReceiptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DonationReceiptServiceTest {

    @Mock
    private DonationReceiptRepository donationReceiptRepository;

    @InjectMocks
    private DonationReceiptService donationReceiptService;

    @Test
    void generateReceipt_ShouldGenerateSequentialNumber() {
        Donation donation = new Donation();
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "RCPT-" + year + "-";

        when(donationReceiptRepository.findMaxReceiptNumberByYear(any())).thenReturn(5);
        when(donationReceiptRepository.save(any(DonationReceipt.class))).thenAnswer(i -> i.getArguments()[0]);

        DonationReceipt receipt = donationReceiptService.generateReceipt(donation);

        assertNotNull(receipt);
        assertEquals(prefix + "000006", receipt.getReceiptNumber());
        verify(donationReceiptRepository).save(any(DonationReceipt.class));
    }
}
