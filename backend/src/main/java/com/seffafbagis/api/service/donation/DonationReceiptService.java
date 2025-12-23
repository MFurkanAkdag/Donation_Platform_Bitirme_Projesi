package com.seffafbagis.api.service.donation;

import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.donation.DonationReceipt;
import com.seffafbagis.api.repository.DonationReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DonationReceiptService {

    private final DonationReceiptRepository donationReceiptRepository;

    @Transactional
    public DonationReceipt generateReceipt(Donation donation) {
        DonationReceipt receipt = new DonationReceipt();
        receipt.setDonation(donation);
        receipt.setIssuedAt(OffsetDateTime.now());

        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "RCPT-" + year + "-";

        // Find last receipt for this year
        Integer maxNum = donationReceiptRepository.findMaxReceiptNumberByYear(prefix + "%");

        long nextNum = (maxNum != null) ? maxNum + 1 : 1;

        String receiptNumber = prefix + String.format("%06d", nextNum);
        receipt.setReceiptNumber(receiptNumber);

        return donationReceiptRepository.save(receipt);
    }

    public DonationReceipt getReceiptByDonation(UUID donationId) {
        return donationReceiptRepository.findByDonationId(donationId)
                .orElse(null);
    }

    public DonationReceipt getReceiptByNumber(String receiptNumber) {
        return donationReceiptRepository.findByReceiptNumber(receiptNumber)
                .orElse(null);
    }
}
