# PHASE 9.0: PAYMENT MODULE (IYZICO INTEGRATION)

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:** Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven

**Current Phase:** 9.0 - Payment Module (Iyzico Integration)

**Previous Phases Completed:**
- Phase 1.0-6.0: Foundation modules ✅
- Phase 7.0: Donation Core ✅
- Phase 8.0: Recurring Donations & Bank Transfers ✅

---

## Objective

Integrate Iyzico payment gateway for credit card processing with 3D Secure support, card token storage for recurring donations, refund processing, and transaction management.

---

## What This Phase Will Solve

1. **Credit Card Payments**: Process donations via Iyzico
2. **3D Secure**: Required by Turkish regulations for card payments
3. **Card Token Storage**: Save cards for recurring donations
4. **Refund Processing**: Handle refund requests
5. **Transaction Logging**: Store all payment details for audit

---

## Iyzico Configuration

Add to `application.yml`:
```yaml
iyzico:
  api-key: ${IYZICO_API_KEY}
  secret-key: ${IYZICO_SECRET_KEY}
  base-url: https://sandbox-api.iyzipay.com  # Use https://api.iyzipay.com for production
  callback-url: ${BASE_URL}/api/v1/payments/callback/3ds
```

Add to `pom.xml`:
```xml
<dependency>
    <groupId>com.iyzipay</groupId>
    <artifactId>iyzipay-java</artifactId>
    <version>2.0.131</version>
</dependency>
```

---

## Files to Create

### 1. Configuration
**Location:** `src/main/java/com/seffafbagis/api/config/IyzicoConfig.java`

Configuration properties class:
- apiKey, secretKey, baseUrl, callbackUrl
- Create Options bean for Iyzico SDK

---

### 2. DTOs - Request
**Location:** `src/main/java/com/seffafbagis/api/dto/request/payment/`

#### PaymentRequest.java
Fields:
- donationId (required, UUID)
- cardHolderName (required)
- cardNumber (required, 16 digits)
- expireMonth (required, 2 digits)
- expireYear (required, 4 digits)
- cvc (required, 3-4 digits)
- saveCard (optional, for recurring)

#### ThreeDSCallbackRequest.java
Fields for Iyzico callback:
- status
- paymentId
- conversationId
- mdStatus (3DS result)

#### RefundPaymentRequest.java
Fields:
- transactionId (required, UUID)
- amount (optional - partial refund, if null = full refund)
- reason (required)

#### SaveCardRequest.java
Fields:
- cardAlias (optional, display name)
- cardHolderName, cardNumber, expireMonth, expireYear (required)

---

### 3. DTOs - Response
**Location:** `src/main/java/com/seffafbagis/api/dto/response/payment/`

#### PaymentResultResponse.java
Fields: success, donationId, transactionId, status, message, receiptNumber (if success)

#### ThreeDSInitResponse.java
Fields: threeDSHtmlContent (HTML form to redirect), paymentId

#### RefundResultResponse.java
Fields: success, transactionId, refundedAmount, message

#### SavedCardResponse.java
Fields: cardToken, cardAlias, cardLastFour, cardBrand, cardFamily

#### TransactionResponse.java
Fields: id, donationId, paymentMethod, provider, providerTransactionId, amount, feeAmount, netAmount, status, cardLastFour, cardBrand, is3dSecure, processedAt

---

### 4. Exception
**Location:** `src/main/java/com/seffafbagis/api/exception/PaymentException.java`

Custom exception for payment errors with:
- errorCode
- errorMessage
- providerErrorCode (from Iyzico)

---

### 5. Mapper
**Location:** `src/main/java/com/seffafbagis/api/dto/mapper/PaymentMapper.java`

Methods:
- toTransactionResponse(Transaction)
- toSavedCardResponse(card info from Iyzico)

---

### 6. Services
**Location:** `src/main/java/com/seffafbagis/api/service/payment/`

#### PaymentService.java

**Main Methods:**
- initializePayment(PaymentRequest) - start 3DS payment flow
- handle3DSCallback(ThreeDSCallbackRequest) - process callback from bank
- processDirectPayment(PaymentRequest) - non-3DS payment (if allowed)
- refundPayment(RefundPaymentRequest) - process refund

**Card Management:**
- saveCard(SaveCardRequest) - save card for recurring
- getSavedCards() - list user's saved cards
- deleteCard(String cardToken) - remove saved card

**Internal:**
- getTransaction(UUID transactionId)
- getTransactionByDonation(UUID donationId)

#### IyzicoService.java

**Low-level Iyzico API calls:**
- create3DSPayment(PaymentRequest, Donation, User) - initiate 3DS
- complete3DSPayment(String paymentId) - complete after callback
- createDirectPayment(PaymentRequest, Donation, User) - non-3DS
- createRefund(Transaction, BigDecimal amount)
- createCardToken(SaveCardRequest, User) - register card
- deleteCardToken(String cardToken, User)
- chargeWithToken(String cardToken, BigDecimal amount, Donation) - for recurring

**Helper Methods:**
- buildBuyer(User) - create Iyzico Buyer object
- buildAddress(User) - create Iyzico Address
- buildPaymentCard(PaymentRequest) - create PaymentCard
- buildBasketItems(Donation) - create basket for Iyzico
- parseIyzicoResponse(response) - extract relevant data

#### TransactionService.java

**Methods:**
- createTransaction(Donation, PaymentRequest, IyzicoResponse)
- updateTransactionStatus(UUID transactionId, String status)
- recordRefund(UUID transactionId, BigDecimal amount)
- getTransactionsByStatus(String status, Pageable)

---

### 7. Controller
**Location:** `src/main/java/com/seffafbagis/api/controller/payment/`

#### PaymentController.java

**Payment Endpoints:**
```
POST /api/v1/payments/initialize          - Start 3DS payment, returns HTML
POST /api/v1/payments/callback/3ds        - 3DS callback from bank (handles redirect)
GET  /api/v1/payments/{transactionId}     - Get transaction status
POST /api/v1/payments/{transactionId}/refund - Request refund
```

**Card Management Endpoints:**
```
POST   /api/v1/payments/cards             - Save card
GET    /api/v1/payments/cards             - List saved cards
DELETE /api/v1/payments/cards/{token}     - Delete saved card
```

---

## Payment Flow (3D Secure)

```
1. User enters card info on frontend
           │
           ▼
2. POST /api/v1/payments/initialize
   - Create Iyzico 3DS request
   - Return HTML form
           │
           ▼
3. Frontend renders HTML (redirects to bank)
           │
           ▼
4. User completes 3DS at bank
           │
           ▼
5. Bank redirects to callback URL
   POST /api/v1/payments/callback/3ds
           │
     ┌─────┴─────┐
     ▼           ▼
  Success     Failure
     │           │
     ▼           ▼
  - Update    - Update donation
    donation    status = FAILED
    status    - Log error
    = COMPLETED
  - Create    
    transaction
  - Update    
    campaign stats
  - Generate  
    receipt
  - Redirect to success page
```

---

## Transaction Recording

Store in Transaction entity:
- providerTransactionId (Iyzico payment ID)
- providerPaymentId (Iyzico conversation ID)
- amount, feeAmount, netAmount
- cardLastFour, cardBrand (Visa, Mastercard, etc.)
- is3dSecure (always true for Turkey)
- rawResponse (JSONB - full Iyzico response for debugging)
- status (from Iyzico)
- errorCode, errorMessage (if failed)

---

## Recurring Payment with Saved Card

```
1. RecurringDonationScheduler calls processRecurringPayment()
           │
           ▼
2. Get saved card token from RecurringDonation
           │
           ▼
3. IyzicoService.chargeWithToken(cardToken, amount)
           │
     ┌─────┴─────┐
     ▼           ▼
  Success     Failure
     │           │
     ▼           ▼
  Complete    Increment
  donation    failure count
```

---

## Security Considerations

1. **NEVER store full card numbers** - Only last 4 digits
2. **NEVER log card details** - Mask in all logs
3. **Use Iyzico card tokens** - For recurring payments
4. **Validate callback signature** - Ensure callback is from Iyzico
5. **HTTPS only** - All payment endpoints
6. **PCI DSS** - Card data goes directly to Iyzico, not stored locally

---

## Error Handling

Common Iyzico error codes to handle:
- `10051` - Insufficient funds
- `10005` - Invalid card
- `10012` - Invalid CVC
- `10034` - Fraud suspected
- `10057` - Card expired

Map to user-friendly messages in Turkish.

---

## Testing Requirements

### Unit Tests
- PaymentServiceTest:
  - Test 3DS flow initialization
  - Test callback processing
  - Test refund processing

- IyzicoServiceTest:
  - Mock Iyzico responses
  - Test error handling

### Integration Tests (Sandbox)
- Full payment flow with test cards
- Refund processing
- Card token save and charge

**Iyzico Test Cards:**
- Success: 5528790000000008 (Mastercard)
- 3DS Required: 4543590000000006 (Visa)
- Fail: 5406670000000009

---

## Success Criteria

- [ ] IyzicoConfig loads credentials correctly
- [ ] All 4 request DTOs created
- [ ] All 5 response DTOs created
- [ ] PaymentException with error codes
- [ ] PaymentService handles 3DS flow
- [ ] IyzicoService makes correct API calls
- [ ] TransactionService records all transactions
- [ ] Card token save/delete works
- [ ] Refund processing works
- [ ] Transaction stores rawResponse as JSONB
- [ ] Error messages user-friendly in Turkish
- [ ] No card numbers logged or stored
- [ ] All endpoints secured appropriately
- [ ] Sandbox tests pass

---

## Result File Requirement

After completing this phase, create:
**Location:** `docs/Emir/step_results/phase_9.0_result.md`

Include:
1. Summary
2. Files created
3. API endpoints table
4. 3DS payment flow description
5. Iyzico configuration notes
6. Security measures implemented
7. Testing results (sandbox)
8. Issues and resolutions
9. Next steps (Phase 10.0)
10. Success criteria checklist

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else
2. **NEVER store full card numbers** - Security critical
3. **Test in sandbox first** - Use test cards
4. **3DS is mandatory** in Turkey for online payments
5. **Log transaction IDs** not card data
6. **Handle timeout** - Iyzico calls may timeout

---

## Dependencies

From Furkan's work:
- SecurityUtils for current user
- ApiResponse for responses
- AuditLogService for logging payment actions

From previous phases:
- DonationService (Phase 7) - for completing donations
- RecurringDonationService (Phase 8) - for card token storage
- Transaction entity (Phase 6)

---

## Estimated Duration

4 days

---

## Next Phase

**Phase 10.0: Evidence Module**
