package com.seffafbagis.api.service.payment;

import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.*;
import com.seffafbagis.api.dto.request.payment.PaymentRequest;
import com.seffafbagis.api.dto.request.payment.SaveCardRequest;
import com.seffafbagis.api.exception.PaymentException;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.donation.Transaction;
import com.seffafbagis.api.entity.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.seffafbagis.api.dto.response.payment.SavedCardResponse; // Using SavedCardResponse instead of creating new DTO for now

@Service
@Slf4j
public class IyzicoService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IyzicoService.class);

    private final Options iyzicoOptions;
    private final String callbackUrl;

    public IyzicoService(Options iyzicoOptions,
            @org.springframework.beans.factory.annotation.Value("${iyzico.callback-url}") String callbackUrl) {
        this.iyzicoOptions = iyzicoOptions;
        this.callbackUrl = callbackUrl;
    }

    public ThreedsInitialize create3DSPayment(PaymentRequest request, Donation donation, User user) {
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setLocale(Locale.TR.getValue());
        paymentRequest.setConversationId(donation.getId().toString());
        paymentRequest.setPrice(donation.getAmount());
        paymentRequest.setPaidPrice(donation.getAmount());
        paymentRequest.setCurrency(Currency.TRY.name());
        paymentRequest.setInstallment(1);
        paymentRequest.setBasketId(donation.getId().toString());
        paymentRequest.setPaymentChannel(PaymentChannel.WEB.name());
        paymentRequest.setPaymentGroup(PaymentGroup.PRODUCT.name());
        paymentRequest.setCallbackUrl(callbackUrl);

        PaymentCard paymentCard = buildPaymentCard(request);
        paymentRequest.setPaymentCard(paymentCard);

        Buyer buyer = buildBuyer(user);
        paymentRequest.setBuyer(buyer);

        Address address = buildAddress(user);
        paymentRequest.setBillingAddress(address);
        paymentRequest.setShippingAddress(address);

        List<BasketItem> basketItems = buildBasketItems(donation);
        paymentRequest.setBasketItems(basketItems);

        ThreedsInitialize threedsInitialize = ThreedsInitialize.create(paymentRequest, iyzicoOptions);

        if (!"success".equals(threedsInitialize.getStatus())) {
            log.error("Iyzico 3DS Init validation failed: {}", threedsInitialize.getErrorMessage());
            throw new PaymentException(threedsInitialize.getErrorMessage(), threedsInitialize.getErrorCode(), false);
        }

        return threedsInitialize;
    }

    public ThreedsPayment complete3DSPayment(String paymentId, String conversationId) {
        CreateThreedsPaymentRequest request = new CreateThreedsPaymentRequest();
        request.setLocale(Locale.TR.getValue());
        request.setConversationId(conversationId);
        request.setPaymentId(paymentId);

        ThreedsPayment threedsPayment = ThreedsPayment.create(request, iyzicoOptions);

        return threedsPayment;
    }

    public Payment createDirectPayment(PaymentRequest request, Donation donation, User user) {
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setLocale(Locale.TR.getValue());
        paymentRequest.setConversationId(donation.getId().toString());
        paymentRequest.setPrice(donation.getAmount());
        paymentRequest.setPaidPrice(donation.getAmount());
        paymentRequest.setCurrency(Currency.TRY.name());
        paymentRequest.setInstallment(1);
        paymentRequest.setBasketId(donation.getId().toString());
        paymentRequest.setPaymentChannel(PaymentChannel.WEB.name());
        paymentRequest.setPaymentGroup(PaymentGroup.PRODUCT.name());

        PaymentCard paymentCard = buildPaymentCard(request);
        paymentRequest.setPaymentCard(paymentCard);

        Buyer buyer = buildBuyer(user);
        paymentRequest.setBuyer(buyer);

        Address address = buildAddress(user);
        paymentRequest.setBillingAddress(address);
        paymentRequest.setShippingAddress(address);

        List<BasketItem> basketItems = buildBasketItems(donation);
        paymentRequest.setBasketItems(basketItems);

        return Payment.create(paymentRequest, iyzicoOptions);
    }

    public Payment chargeWithToken(String cardToken, BigDecimal amount, Donation donation, User user) {
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setLocale(Locale.TR.getValue());
        paymentRequest.setConversationId(donation.getId().toString());
        paymentRequest.setPrice(amount);
        paymentRequest.setPaidPrice(amount);
        paymentRequest.setCurrency(Currency.TRY.name());
        paymentRequest.setInstallment(1);
        paymentRequest.setBasketId(donation.getId().toString());
        paymentRequest.setPaymentChannel(PaymentChannel.WEB.name());
        paymentRequest.setPaymentGroup(PaymentGroup.PRODUCT.name());

        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCardToken(cardToken);
        paymentRequest.setPaymentCard(paymentCard);

        Buyer buyer = buildBuyer(user);
        paymentRequest.setBuyer(buyer);

        Address address = buildAddress(user);
        paymentRequest.setBillingAddress(address);
        paymentRequest.setShippingAddress(address);

        List<BasketItem> basketItems = buildBasketItems(donation);
        paymentRequest.setBasketItems(basketItems);

        return Payment.create(paymentRequest, iyzicoOptions);
    }

    public Refund createRefund(Transaction transaction, BigDecimal amount) {
        CreateRefundRequest request = new CreateRefundRequest();
        request.setLocale(Locale.TR.getValue());
        request.setConversationId(transaction.getProviderTransactionId());
        request.setPaymentTransactionId(transaction.getProviderTransactionId());

        if (amount != null) {
            request.setPrice(amount);
        } else {
            request.setPrice(transaction.getAmount()); // Full refund
        }

        return Refund.create(request, iyzicoOptions);
    }

    public SavedCardResponse createCardToken(SaveCardRequest request, User user) {
        CreateCardRequest cardRequest = new CreateCardRequest();
        cardRequest.setLocale(Locale.TR.getValue());
        cardRequest.setConversationId(UUID.randomUUID().toString());

        // Iyzico requires email to group cards under a user
        cardRequest.setEmail(user.getEmail());

        CardInformation cardInformation = new CardInformation();
        cardInformation.setCardAlias(request.getCardAlias());
        cardInformation.setCardHolderName(request.getCardHolderName());
        cardInformation.setCardNumber(request.getCardNumber());
        cardInformation.setExpireMonth(request.getExpireMonth());
        cardInformation.setExpireYear(request.getExpireYear());

        cardRequest.setCard(cardInformation);

        Card card = Card.create(cardRequest, iyzicoOptions);

        if (!"success".equals(card.getStatus())) {
            throw new PaymentException(card.getErrorMessage(), card.getErrorCode(), false);
        }

        SavedCardResponse cardInfo = new SavedCardResponse();
        cardInfo.setCardAlias(card.getCardAlias());
        cardInfo.setCardToken(card.getCardToken());
        cardInfo.setCardBrand(card.getCardType());
        cardInfo.setCardFamily(card.getCardFamily());
        // cardInfo.setCardLastFour(card.getBinNumber());

        return cardInfo;
    }

    public List<SavedCardResponse> getUserCards(String cardUserKey) {
        RetrieveCardListRequest request = new RetrieveCardListRequest();
        request.setLocale(Locale.TR.getValue());
        request.setCardUserKey(cardUserKey);
        request.setConversationId(UUID.randomUUID().toString());

        CardList cardList = CardList.retrieve(request, iyzicoOptions);

        if (!"success".equals(cardList.getStatus())) {
            throw new PaymentException(cardList.getErrorMessage(), cardList.getErrorCode(), false);
        }

        return cardList.getCardDetails().stream()
                .map(card -> {
                    SavedCardResponse cardInfo = new SavedCardResponse();
                    cardInfo.setCardAlias(card.getCardAlias());
                    cardInfo.setCardToken(card.getCardToken());
                    // cardInfo.setCardUserKey(card.getCardUserKey()); // Internal use
                    // Mapping other fields
                    cardInfo.setCardBrand(card.getCardType());
                    cardInfo.setCardFamily(card.getCardFamily());
                    cardInfo.setCardLastFour(card.getBinNumber()); // Approximation or just use what we have
                    return cardInfo;
                })
                .collect(Collectors.toList());
    }

    public void deleteCardToken(String cardToken, String cardUserKey) {
        DeleteCardRequest request = new DeleteCardRequest();
        request.setLocale(Locale.TR.getValue());
        request.setCardToken(cardToken);
        request.setCardUserKey(cardUserKey);

        Card.delete(request, iyzicoOptions);
    }

    private PaymentCard buildPaymentCard(PaymentRequest request) {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCardHolderName(request.getCardHolderName());
        paymentCard.setCardNumber(request.getCardNumber());
        paymentCard.setExpireMonth(request.getExpireMonth());
        paymentCard.setExpireYear(request.getExpireYear());
        paymentCard.setCvc(request.getCvc());
        paymentCard.setRegisterCard(request.isSaveCard() ? 1 : 0);
        return paymentCard;
    }

    private Buyer buildBuyer(User user) {
        Buyer buyer = new Buyer();
        buyer.setId(user.getId().toString());
        if (user.getProfile() != null) {
            buyer.setName(user.getProfile().getFirstName());
            buyer.setSurname(user.getProfile().getLastName());
        } else {
            // Fallback if profile is missing
            buyer.setName("Guest");
            buyer.setSurname("User");
        }
        buyer.setEmail(user.getEmail());
        buyer.setIdentityNumber("11111111111");
        buyer.setRegistrationAddress("Not provided");
        buyer.setIp("127.0.0.1");
        buyer.setCity("Istanbul");
        buyer.setCountry("Turkey");
        return buyer;
    }

    private Address buildAddress(User user) {
        Address address = new Address();
        if (user.getProfile() != null) {
            address.setContactName(user.getProfile().getFirstName() + " " + user.getProfile().getLastName());
        } else {
            address.setContactName("Guest User");
        }
        address.setCity("Istanbul");
        address.setCountry("Turkey");
        address.setAddress("Not provided");
        return address;
    }

    private List<BasketItem> buildBasketItems(Donation donation) {
        List<BasketItem> basketItems = new ArrayList<>();
        BasketItem firstBasketItem = new BasketItem();
        firstBasketItem.setId(donation.getId().toString());
        firstBasketItem.setName("Donation - " + donation.getCampaign().getTitle());
        firstBasketItem.setCategory1("Donation");
        firstBasketItem.setPrice(donation.getAmount());
        firstBasketItem.setItemType(BasketItemType.VIRTUAL.name());
        basketItems.add(firstBasketItem);
        return basketItems;
    }
}
