# Payment Session & Guest (Anonim) Donation Analizi

**Oluşturulma Tarihi:** 26 Aralık 2025  
**Son Durum:** Tasarım ve Backend implementasyonu tamamlandı, Frontend entegrasyonu bekliyor

---

## 📋 Genel Bakış

Bu projede **Payment Session** (sepet/cart) ve **Guest Donation** (anonim bağış) özellikleri tasarlandı ve büyük ölçüde implement edildi. Bu doküman mevcut durumu, yapılan işleri ve kalan görevleri detaylı şekilde açıklamaktadır.

---

## ✅ TAMAMLANAN İŞLER

### 1. Payment Session (Sepet) - Backend ✅

#### 1.1 Database Schema
- ✅ **V29 Migration**: `payment_sessions` tablosu oluşturuldu
- ✅ **V30 Migration**: `cart_items` (JSONB) kolonu eklendi
- ✅ `donations` ve `transactions` tablolarına `payment_session_id` foreign key eklendi

#### 1.2 Entity & Enums
- ✅ **PaymentSession.java**: Ana entity tamamlandı
  - User ilişkisi (nullable - guest için)
  - CartItem inner class (JSONB)
  - Business methods (addCartItem, removeCartItem, clearCart, calculateCartTotal)
- ✅ **PaymentSessionStatus enum**: PENDING, PROCESSING, COMPLETED, FAILED, EXPIRED

#### 1.3 Repository & Service
- ✅ **PaymentSessionRepository**: Temel CRUD + custom queries
- ✅ **PaymentSessionService**: 
  - `getOrCreateActiveSession()` - Kullanıcı için aktif sepet getir/oluştur
  - `addItemToCart()` - Sepete ürün ekle
  - `removeItemFromCart()` - Sepetten ürün çıkar
  - `checkout()` - Sepetteki itemlardan donation oluştur, receipt üret
  - `cleanupExpiredSessions()` - Süresi dolmuş sepetleri temizle

#### 1.4 DTO'lar
- ✅ **AddCartItemRequest**: Sepete item eklemek için
- ✅ Validation kuralları tanımlandı

---

### 2. Guest Donation (Anonim Bağış) - Backend ✅

#### 2.1 Controller
- ✅ **GuestDonationController**
  - `POST /api/v1/guest/checkout` endpoint oluşturuldu
  - Swagger dokümantasyonu eklendi
  - Authentication gerektirmiyor (public endpoint)

#### 2.2 Service
- ✅ **GuestDonationService**
  - `processGuestCheckout()`: Ana checkout işlemi
  - Her cart item için `Donation` oluşturuluyor
  - `donor_id = NULL` (anonim kullanıcılar için)
  - Guest bilgileri donation field'larına kaydediliyor:
    - `donorDisplayName` ← guestName
    - Anonim olup olmadığı `isAnonymous` flag'i ile kontrol ediliyor
  - Receipt otomatik oluşturuluyor
  - **NOT**: Payment gateway entegrasyonu TODO olarak işaretlenmiş (şimdilik COMPLETED olarak kaydediliyor)

#### 2.3 DTO'lar
- ✅ **GuestCheckoutRequest**
  - Guest bilgileri: email, name, phone
  - Cart items listesi
  - Payment details (card bilgileri)
  - Optional: donorMessage, isAnonymous
  - Tüm validation kuralları tanımlandı
  
- ✅ **GuestCheckoutResponse**
  - Success bilgisi
  - Oluşturulan donation'lar ve receipt'ler
  - Her donation için: receiptId, campaignId, amount, receiptNumber, receiptPdfUrl
  - Total amount ve guest email

---

### 3. Frontend - Kısmi ✅

#### 3.1 Cart Context
- ✅ **CartContext.tsx**: localStorage tabanlı sepet yönetimi
  - `addToCart()`, `removeFromCart()`, `updateCartItem()`, `clearCart()`
  - `getTotalAmount()` - toplam hesaplama
  - localStorage'a otomatik kaydetme/yükleme
  - **Guest kullanıcılar için hazır!**

#### 3.2 Mevcut Checkout Form
- ✅ Checkout formu zaten mevcut ve gerekli bilgileri topluyor:
  - Full Name, Email, Phone
  - Payment bilgileri (kart detayları)
  - Optional: Country, Billing Address

---

### 4. Dokümantasyon ✅

- ✅ **guest_donation_flow_demo.html**: Guest donation akışını gösteren demo/açıklama sayfası
  - Adım adım flow gösterimi
  - Guest vs Logged-in user karşılaştırması
  - Backend/Frontend yapılacak işler listesi
  - API request/response örnekleri

---

## ⏳ TAMAMLANMAYAN / EKSIK İŞLER

### 1. Backend - Eksikler

#### 1.1 Payment Gateway Entegrasyonu
- ❌ **GuestDonationService** içinde payment işleme TODO olarak bırakılmış
- ❌ `GuestCheckoutRequest.PaymentDetailsRequest` bilgileri henüz payment gateway'e gönderilmiyor
- ❌ Şu anda tüm donation'lar direkt `DonationStatus.COMPLETED` olarak kaydediliyor
- 🔧 **Yapılması Gereken:**
  ```java
  // GuestDonationService.java içinde
  // TODO: Process payment with payment gateway
  // PaymentResultResponse paymentResult = paymentService.processPayment(...)
  // if (paymentResult.isSuccess()) {
  //     donation.setStatus(DonationStatus.COMPLETED);
  // } else {
  //     donation.setStatus(DonationStatus.FAILED);
  // }
  ```

#### 1.2 Email Service Entegrasyonu
- ❌ Guest kullanıcılara receipt email gönderilmiyor
- 🔧 **Yapılması Gereken:**
  ```java
  // Receipt oluşturulduktan sonra
  emailService.sendReceiptEmail(request.getGuestEmail(), receipt);
  ```

#### 1.3 Campaign Stats Güncelleme
- ⚠️ Guest donation'larda campaign istatistikleri güncelleniyor mu? Kontrol edilmeli.

#### 1.4 Transaction Kaydı
- ❌ `PaymentSession` ile `Transaction` ilişkisi var ama Guest checkout'ta transaction oluşturuluyor mu?
- ⚠️ Guest donation'lar için `payment_session_id` NULL mu yoksa guest için özel session mi oluşturuluyor?

---

### 2. Frontend - Eksikler

#### 2.1 API Service
- ❌ **guestDonationService.ts** dosyası oluşturulmadı
- 🔧 **Yapılması Gereken:**
  ```typescript
  // services/guestDonationService.ts
  export async function guestCheckout(request: GuestCheckoutRequest) {
    const response = await fetch('/api/v1/guest/checkout', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    });
    return response.json();
  }
  ```

#### 2.2 Checkout Page Logic
- ❌ Checkout sayfasında **authentication kontrolü** yok
- ❌ Guest vs Logged-in user için farklı API endpoint kullanımı implement edilmemiş
- 🔧 **Yapılması Gereken:**
  ```typescript
  // pages/checkout.tsx veya app/checkout/page.tsx
  const handleCheckout = async () => {
    if (!isAuthenticated()) {
      // Guest checkout
      await guestCheckout({
        guestEmail: formData.email,
        guestName: formData.name,
        guestPhone: formData.phone,
        cartItems: cart,
        paymentDetails: formData.paymentDetails,
        donorMessage: formData.message,
        isAnonymous: formData.isAnonymous
      });
    } else {
      // Logged-in user checkout
      await checkoutCart(); // Mevcut API
    }
  };
  ```

#### 2.3 Cart Backend Sync
- ⚠️ Logged-in kullanıcılar için cart backend'de (PaymentSession) tutulmalı
- ⚠️ Guest kullanıcılar için localStorage kullanılmalı
- ❌ Login olduktan sonra localStorage'daki cart'ı backend'e senkronize etme mekanizması yok
- 🔧 **Yapılması Gereken:**
  ```typescript
  // Kullanıcı login olduktan sonra
  const syncCartToBackend = async () => {
    const localCart = JSON.parse(localStorage.getItem('donationCart') || '[]');
    for (const item of localCart) {
      await addItemToCartAPI(item.campaignId, item.amount);
    }
    localStorage.removeItem('donationCart');
  };
  ```

---

### 3. Test - Eksikler

- ❌ **GuestDonationServiceTest** yazılmadı
- ❌ **PaymentSessionServiceTest** yazılmadı
- ❌ Integration testleri eksik
- ❌ Frontend unit testleri yok

---

### 4. Security & Validation

- ⚠️ **Rate Limiting**: Guest checkout endpoint'i için rate limiting gerekli (abuse önlemek için)
- ⚠️ **CAPTCHA**: Bot saldırılarına karşı reCAPTCHA eklenmeli
- ⚠️ **Email Validation**: Email gerçekten kullanıcıya mı ait? (Email verification olmadan receipt gönderilebilir mi?)

---

## 📊 Yapılma Durumu Özeti

| Modül | Durum | Tamamlanma % |
|-------|-------|--------------|
| **Backend - Database Schema** | ✅ Tamamlandı | 100% |
| **Backend - PaymentSession Entity/Service** | ✅ Tamamlandı | 100% |
| **Backend - GuestDonation Entity/Service** | ⚠️ Kısmi | 70% |
| **Backend - Payment Gateway** | ❌ Eksik | 0% |
| **Backend - Email Service** | ❌ Eksik | 0% |
| **Frontend - Cart (localStorage)** | ✅ Tamamlandı | 100% |
| **Frontend - Checkout Logic** | ❌ Eksik | 20% |
| **Frontend - Guest API Service** | ❌ Eksik | 0% |
| **Testing** | ❌ Eksik | 0% |
| **Dokümantasyon** | ✅ Tamamlandı | 100% |

**GENEL TAMAMLANMA:** ~55%

---

## 🎯 Öncelikli Yapılacaklar Listesi

### High Priority (P0)
1. ✅ **Frontend Checkout Page** - Authentication kontrolü ve API endpoint seçimi
2. ✅ **Frontend Guest API Service** - `guestCheckout()` fonksiyonu
3. ⚠️ **Payment Gateway Entegrasyonu** - Gerçek ödeme işleme (production için kritik)

### Medium Priority (P1)
4. ⚠️ **Email Service** - Guest kullanıcılara receipt gönderimi
5. ⚠️ **Cart Sync** - Login sonrası localStorage → backend sync
6. ⚠️ **Transaction Kaydı** - Guest checkout'ta transaction oluşturma

### Low Priority (P2)
7. ⚠️ **Rate Limiting** - Guest endpoint için koruma
8. ⚠️ **Testing** - Unit ve integration testleri
9. ⚠️ **CAPTCHA** - Bot koruması

---

## 🔍 Teknik Detaylar

### Guest Donation Flow (Backend)

```java
// 1. Request alınır
GuestCheckoutRequest request = {...};

// 2. Campaign'ler validate edilir
validateCampaigns(request.getCartItems());

// 3. Her cart item için Donation oluşturulur
for (CartItemRequest item : request.getCartItems()) {
    Donation donation = new Donation();
    donation.setCampaign(campaign);
    donation.setDonor(null); // 🔑 Guest için NULL!
    donation.setAmount(item.getAmount());
    donation.setDonorDisplayName(request.getGuestName());
    donation.setIsAnonymous(request.getIsAnonymous());
    donation.setStatus(DonationStatus.COMPLETED); // TODO: Payment sonrası set et
    
    donationRepository.save(donation);
    
    // 4. Receipt oluşturulur
    Receipt receipt = receiptService.createReceipt(donation);
}

// 5. Response dönülür
return GuestCheckoutResponse.builder()
    .donations(donationInfos)
    .totalAmount(totalAmount)
    .guestEmail(request.getGuestEmail())
    .build();
```

### Database İlişkileri

```
users (id) 
   ↓ (nullable)
payment_sessions (id, user_id, cart_items JSONB, status)
   ↓
donations (id, payment_session_id, donor_id, campaign_id, amount)
   ↓
receipts (id, donation_id, barcode_data, pdf_path)

transactions (id, payment_session_id, status, amount)
```

**Guest Donation Özelliği:**
- `donations.donor_id = NULL`
- `donations.donorDisplayName` = Form'dan gelen isim
- `payment_sessions.user_id = NULL` (veya guest için özel session?)

---

## 🎨 Frontend Flow Tasarımı

```
1. User adds items to cart
   ↓
2. localStorage.setItem('donationCart', cart)
   ↓
3. User goes to Checkout
   ↓
4. if (!isAuthenticated())
      ├── Guest Flow
      │   ├── Show full form (name, email, phone, card)
      │   ├── POST /api/v1/guest/checkout
      │   └── Receipt → Email
      │
      └── Logged-in Flow
          ├── Auto-fill user info
          ├── POST /api/v1/cart/checkout
          └── Receipt → User dashboard + Email
```

---

## 📝 Notlar

1. **Mevcut checkout formu kullanılabilir** - Sadece backend endpoint'i değiştirilmeli
2. **localStorage cart** zaten çalışıyor - Guest kullanıcılar için hazır
3. **Backend altyapısı sağlam** - Entity, Service, Repository hepsi hazır
4. **Payment gateway** en büyük eksik - Production'a geçmeden önce mutlaka tamamlanmalı
5. **Email servisi** mevcut (diğer özellikler için kullanılıyor) - Guest için de kullanılabilir

---

## 🚀 Hızlı Başlangıç Rehberi

### Backend Test (Swagger UI)

1. Backend'i başlat
2. `http://localhost:8080/swagger-ui.html` aç
3. `Guest Donations` controller'ına git
4. `POST /api/v1/guest/checkout` endpoint'ini test et
5. Sample request body:
   ```json
   {
     "guestEmail": "test@example.com",
     "guestName": "Test User",
     "guestPhone": "+905551234567",
     "cartItems": [
       {
         "campaignId": "valid-campaign-uuid",
         "amount": 100.00,
         "currency": "TRY"
       }
     ],
     "paymentDetails": {
       "cardHolderName": "Test User",
       "cardNumber": "1234567812345678",
       "expireMonth": "12",
       "expireYear": "2026",
       "cvc": "123"
     },
     "isAnonymous": false
   }
   ```

### Frontend Development

1. `CartContext` zaten kullanıma hazır
2. Checkout sayfasında authentication kontrolü ekle
3. Guest için yeni API service oluştur
4. Form submit'te doğru endpoint'e istek at

---

**Son Güncelleme:** 26 Aralık 2025, 17:30
**Hazırlayan:** AI Assistant
**Durum:** Payment Session tasarım ve backend tamamlandı, frontend entegrasyonu bekleniyor
