package com.seffafbagis.api.enums;

/**
 * Status of a donation.
 */
public enum DonationStatus {
    PENDING, // Bekleniyor - Payment initiated but not yet confirmed
    COMPLETED, // Tamamlandı - Payment successfully processed
    FAILED, // Başarısız - Payment failed
    REFUNDED // İade edildi - Payment refunded
}
