package com.seffafbagis.api.dto.request.payment;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for card information in payment requests.
 */
public class CardInfoRequest {

    @NotBlank(message = "Kart sahibi adı gereklidir")
    private String cardHolderName;

    @NotBlank(message = "Kart numarası gereklidir")
    private String cardNumber;

    @NotBlank(message = "Son kullanma ayı gereklidir")
    private String expireMonth;

    @NotBlank(message = "Son kullanma yılı gereklidir")
    private String expireYear;

    @NotBlank(message = "CVC gereklidir")
    private String cvc;

    private Boolean saveCard = false;

    // Getters and Setters

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

    public Boolean getSaveCard() {
        return saveCard;
    }

    public void setSaveCard(Boolean saveCard) {
        this.saveCard = saveCard;
    }
}
