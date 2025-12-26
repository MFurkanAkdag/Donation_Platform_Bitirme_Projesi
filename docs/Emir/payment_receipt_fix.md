# Payment Session & Cart System Refactoring

**Date:** 2025-12-25  
**Phase:** Payment & Receipt System Implementation

## Overview

Backend'deki cart ve payment session sistemini yeniden yapılandırdık. Önceden donation'lar direkt oluşturuluyordu, şimdi kullanıcılar sepete item ekleyip topluca checkout yapabiliyor. Receipt sistemi de tamamlandı.

---

## Changes Summary

### 1. PaymentSession Entity Refactoring

**Problem:**  
PaymentSession sadece tek bir ödeme işlemini takip ediyordu. Kullanıcılar birden fazla kampanyaya bağış yapmak istediğinde, her biri için ayrı session oluşturmak gerekiyordu.

**Solution:**  
PaymentSession'a **cart items** özelliği ekledik. Artık bir session birden fazla cart item'ı (campaignId + amount) saklayabiliyor.

#### Database Changes

**[NEW] V30__add_cart_items_column.sql**
```sql
ALTER TABLE payment_sessions 
ADD COLUMN cart_items JSONB DEFAULT '[]'::jsonb;

COMMENT ON COLUMN payment_sessions.cart_items IS 
'Cart items before checkout: [{campaignId: UUID, amount: decimal, currency: string}]';
```

#### Entity Changes

**[MODIFIED] PaymentSession.java**
```java
@Type(JsonBinaryType.class)
@Column(name = "cart_items", columnDefinition = "jsonb")
private List<CartItem> cartItems = new ArrayList<>();

// CartItem inner class
public static class CartItem {
    private UUID campaignId;
    private BigDecimal amount;
    private String currency;
}
```

---

### 2. Cart API Endpoints

#### New DTOs

**[NEW] AddCartItemRequest.java**
```java
public class AddCartItemRequest {
    @NotNull
    private UUID campaignId;
    
    @NotNull
    @DecimalMin("1.0")
    private BigDecimal amount;
    
    private String currency = "TRY";
}
```

**[NEW] CartItemResponse.java**
```java
public class CartItemResponse {
    private UUID campaignId;
    private String campaignTitle;
    private BigDecimal amount;
    private String currency;
}
```

#### Controller Endpoints

**[MODIFIED] CartController.java**

Updated all endpoints to use cart items instead of direct donations:

1. **GET /api/v1/cart** - Get current cart with items
   - Returns: sessionId, totalAmount, items[], itemCount
   - **Auth Required:** ✅ isAuthenticated()

2. **POST /api/v1/cart/items** - Add item to cart
   - Body: `{ campaignId, amount, currency }`
   - **Auth Required:** ✅ isAuthenticated()

3. **DELETE /api/v1/cart/items/{campaignId}** - Remove item from cart
   - **Auth Required:** ✅ isAuthenticated()

4. **POST /api/v1/cart/checkout** - Checkout (creates donations & receipts)
   - Converts all cart items → donations
   - Generates receipts for each donation
   - **Auth Required:** ✅ isAuthenticated()

---

### 3. PaymentSessionService Updates

**[MODIFIED] PaymentSessionService.java**

#### New Methods Added:

```java
// Get or create active session for user
public PaymentSession getOrCreateActiveSession(UUID userId)

// Add item to cart
public void addItemToCart(UUID userId, AddCartItemRequest request)

// Remove item from cart
public void removeItemFromCart(UUID userId, UUID campaignId)

// Checkout - creates donations from cart items
public void checkout(UUID sessionId)
```

#### Checkout Flow:

```java
public void checkout(UUID sessionId) {
    PaymentSession session = getSession(sessionId);
    
    // For each cart item, create a donation
    session.getCartItems().forEach(item -> {
        Donation donation = new Donation();
        donation.setCampaign(campaignRepo.findById(item.getCampaignId()));
        donation.setDonor(user);
        donation.setAmount(item.getAmount());
        donation.setCurrency(item.getCurrency());
        donation.setStatus(DonationStatus.COMPLETED);
        donation.setPaymentSession(session);
        
        // Save donation
        donationRepo.save(donation);
        
        // Generate receipt
        receiptService.generateReceipt(donation);
    });
    
    // Clear cart and mark session as completed
    session.getCartItems().clear();
    session.setStatus(PaymentSessionStatus.COMPLETED);
    sessionRepo.save(session);
}
```

---

### 4. Receipt System Implementation

#### Receipt Component Demo

**[NEW] receipt_components_demo.html**

Created a visual demo page showing:
- Receipt card design
- QR code/barcode placeholder
- Campaign information display
- Donation amount and date
- Download/Share buttons
- Email receipt functionality preview

**Demo Features:**
- Modern, clean UI with gradient backgrounds
- Responsive design
- Receipt card with campaign image
- Barcode display area
- Action buttons (Download PDF, Share, Email)
- Security badges (encrypted, verified, instant)

---

## Data Flow: Cart → Checkout → Donation → Receipt

```
1. User adds items to cart
   └─> POST /api/v1/cart/items
   └─> PaymentSession.cartItems[] updated

2. User views cart
   └─> GET /api/v1/cart
   └─> Returns cart items with campaign details

3. User clicks checkout
   └─> POST /api/v1/cart/checkout
   └─> For each cart item:
       ├─> Create Donation entity
       ├─> Set donor, campaign, amount
       ├─> Save to database
       └─> Generate Receipt
           ├─> Create Receipt entity
           ├─> Generate barcode data
           ├─> Link to donation
           └─> Send email to donor

4. Cart cleared, session completed
   └─> PaymentSession.cartItems = []
   └─> PaymentSession.status = COMPLETED
```

---

## Files Changed

### Backend - Entities
- ✅ `PaymentSession.java` - Added cart_items field and CartItem inner class
- ✅ `Donation.java` - Already supports anonymous (donor nullable)

### Backend - DTOs
- ✅ `AddCartItemRequest.java` - NEW
- ✅ `CartItemResponse.java` - NEW

### Backend - Controllers
- ✅ `CartController.java` - Refactored to use cart items

### Backend - Services
- ✅ `PaymentSessionService.java` - Added cart management methods

### Backend - Database Migrations
- ✅ `V29__add_payment_sessions.sql` - Payment sessions table (already existed)
- ✅ `V30__add_cart_items_column.sql` - NEW - Added cart_items JSONB column

### Frontend - Demo
- ✅ `receipt_components_demo.html` - Receipt UI demo page

---

## Testing Performed

### Manual Testing via Swagger UI

1. **Add items to cart:**
   ```bash
   POST /api/v1/cart/items
   {
     "campaignId": "uuid-1",
     "amount": 100.00,
     "currency": "TRY"
   }
   ```

2. **View cart:**
   ```bash
   GET /api/v1/cart
   # Response: {sessionId, totalAmount: 100, items: [...], itemCount: 1}
   ```

3. **Checkout:**
   ```bash
   POST /api/v1/cart/checkout
   # Creates donations & receipts
   ```

4. **Verify in database:**
   ```sql
   SELECT * FROM donations WHERE payment_session_id = 'session-uuid';
   SELECT * FROM receipts WHERE donation_id IN (...);
   ```

---

## Git Commit

**Commit:** `4d64d87`  
**Message:**
```
feat: Add cart items support and receipt demo

- Added cart items functionality with AddCartItemRequest and CartItemResponse DTOs
- Updated PaymentSession to store cart items before checkout
- Added V30 migration for cart_items JSONB column
- Created receipt components HTML demo for testing
- Updated CartController and PaymentSessionService for cart management
```

**Files changed:** 7 files, 533 insertions, 125 deletions

---

## Known Limitations & Future Work

### Current Limitations:

1. **No Guest/Anonymous Donations Yet**
   - All cart endpoints require `@PreAuthorize("isAuthenticated()")`
   - Guest users cannot add to cart or checkout
   - **Planned Fix:** Separate guest checkout endpoint (see next section)

2. **Cart only in Backend**
   - Frontend still uses localStorage for cart
   - No sync with backend cart yet
   - **Planned:** Update CartContext to use backend API

3. **No Cart Persistence Across Devices**
   - Cart tied to PaymentSession per user
   - If user logs in from different device, cart is separate
   - **Future:** Could merge sessions on login

### Next Steps:

1. **Guest Donation Support** (Planned - see implementation_plan.md)
   - Create `/api/v1/guest/checkout` endpoint
   - Allow checkout without authentication
   - Collect email/name at checkout time
   - Create donations with `donor_id = NULL`

2. **Frontend Cart Integration**
   - Update `CartContext.tsx` to use backend API
   - Create `cartService.ts` for API calls
   - Sync localStorage cart with backend on login
   - Update cart page to fetch from backend

3. **Payment Integration**
   - Connect checkout to actual payment gateway (Iyzico/PayTR)
   - Handle 3DS redirect flow
   - Process payment before creating donations
   - Update donation status based on payment result

---

## Architecture Decisions

### Why JSONB for cart_items?

**Pros:**
- Flexible schema - can add fields without migration
- Native PostgreSQL support with indexes
- Easy to query with `@>` operator
- No need for separate cart_items table

**Cons:**
- Less normalized than separate table
- Harder to enforce referential integrity
- Limited type safety in Java (handled by Hibernate @Type)

**Decision:** JSONB is acceptable because:
- Cart items are temporary (cleared after checkout)
- No complex queries needed on cart items
- Simplifies schema and reduces joins

### Why separate Guest endpoint vs modifying existing?

**Alternative 1:** Make existing CartController endpoints public
- **Pros:** Less code duplication
- **Cons:** Authentication logic becomes complex, harder to track guest vs user

**Alternative 2:** Separate GuestDonationController (CHOSEN)
- **Pros:** Clear separation, easier to maintain, different business logic
- **Cons:** Some code duplication (can be extracted to shared service)

**Decision:** Separate endpoint is cleaner and more maintainable.

---

## Receipt System Details

### Receipt Entity Fields:
- `donation_id` (FK to donations) - One-to-one relationship
- `barcode_data` - Unique receipt identifier (e.g., "RCP-2024-001")
- `pdf_url` - Path to generated PDF receipt
- `sent_at` - When email was sent to donor
- `downloaded_at` - When donor downloaded PDF

### Receipt Generation Flow:
1. Donation created and saved
2. `ReceiptService.generateReceipt(donation)` called
3. Receipt entity created with unique barcode
4. PDF generated (future: using iText or similar)
5. Email sent to donor with PDF attachment
6. Receipt saved to database

---

## Conclusion

Successfully refactored the payment system to support shopping cart functionality. Users can now:
- ✅ Add multiple campaigns to cart
- ✅ View cart with total amount
- ✅ Remove items before checkout
- ✅ Checkout all items at once
- ✅ Receive receipts for each donation

**Next Priority:** Implement guest/anonymous donation support to allow donations without registration.
