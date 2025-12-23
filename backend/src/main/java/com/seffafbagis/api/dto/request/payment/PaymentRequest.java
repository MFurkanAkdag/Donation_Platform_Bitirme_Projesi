package com.seffafbagis.api.dto.request.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Donation ID is required")
    private UUID donationId;

    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;

    @NotBlank(message = "Card number is required")
    @Size(min = 16, max = 16, message = "Card number must be 16 digits")
    @Pattern(regexp = "\\d+", message = "Card number must contain only digits")
    private String cardNumber;

    @NotBlank(message = "Expire month is required")
    @Size(min = 2, max = 2, message = "Expire month must be 2 digits")
    @Pattern(regexp = "(0[1-9]|1[0-2])", message = "Expire month must be between 01 and 12")
    private String expireMonth;

    @NotBlank(message = "Expire year is required")
    @Size(min = 4, max = 4, message = "Expire year must be 4 digits")
    @Pattern(regexp = "\\d{4}", message = "Expire year must be a valid year")
    private String expireYear;

    @NotBlank(message = "CVC is required")
    @Size(min = 3, max = 4, message = "CVC must be 3 or 4 digits")
    @Pattern(regexp = "\\d+", message = "CVC must contain only digits")
    private String cvc;

    private boolean saveCard;

    public UUID getDonationId() {
        return donationId;
    }

    public void setDonationId(UUID donationId) {
        this.donationId = donationId;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpireMonth() {
        return expireMonth;
    }

    public void setExpireMonth(String expireMonth) {
        this.expireMonth = expireMonth;
    }

    public String getExpireYear() {
        return expireYear;
    }

    public void setExpireYear(String expireYear) {
        this.expireYear = expireYear;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public boolean isSaveCard() {
        return saveCard;
    }

    public void setSaveCard(boolean saveCard) {
        this.saveCard = saveCard;
    }
}
