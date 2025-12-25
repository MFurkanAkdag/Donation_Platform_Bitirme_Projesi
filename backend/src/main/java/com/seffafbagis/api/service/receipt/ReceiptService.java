package com.seffafbagis.api.service.receipt;

import com.seffafbagis.api.entity.Receipt;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;

    @Transactional
    public Receipt createReceipt(Donation donation) {
        String barcode = generateBarcode(donation);

        Receipt receipt = Receipt.builder()
                .donation(donation)
                .barcodeData(barcode)
                .build();

        return receiptRepository.save(receipt);
    }

    public Optional<Receipt> verifyReceipt(String barcodeData) {
        return receiptRepository.findByBarcodeData(barcodeData);
    }

    private String generateBarcode(Donation donation) {
        // Format: SB-{Year}-{UniquePart}
        // Example: SB-2024-839210
        String year = DateTimeFormatter.ofPattern("yyyy").format(donation.getCreatedAt().toLocalDateTime());
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "SB-" + year + "-" + uniquePart;
    }
}
