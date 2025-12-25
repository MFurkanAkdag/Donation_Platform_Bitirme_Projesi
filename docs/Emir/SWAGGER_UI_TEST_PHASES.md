# Swagger UI - Manuel Test Senaryoları v3 (Phase 18 Final)

Bu döküman, Şeffaf Bağış Platformu'nun **Phase 18 (Receipt Verification)** kapsamında doğrulanan uçtan uca test akışını içerir.

> [!TIP]
> **Hızlı Test İçin Hazır Kullanıcılar**
> Test için gerekli kullanıcı (Admin, Vakıf, Bağışçı) bilgileri `TEST_USERS.md` dosyasında mevcuttur.

---

## 🚀 Tam Akış Senaryosu: Vakıf Kurulumundan Makbuza

Bu senaryo sıfırdan başlayarak bir vakfın kurulmasını, kampanya açmasını ve bir bağışçının bağış yapıp makbuz almasını kapsar.

### Adım 1: Kullanıcı Kaydı & Organizasyon Kurulumu
1.  **Vakıf Kaydı**: `POST /auth/register` ile vakıf kullanıcısı oluştur.
2.  **Login**: `POST /auth/login` ile token al ve Authorize ol.
3.  **Organizasyon Oluştur**: `POST /organizations` ile vakıf profili oluştur (Status: PENDING).
4.  **Admin Onayı**: Admin token'ı ile `PUT /admin/organizations/{id}/verify` (Status: APPROVED).

### Adım 2: Kampanya Yönetimi
1.  **Kampanya Oluştur**: Vakıf token'ı ile `POST /campaigns` (Status: DRAFT).
2.  **Onaya Gönder**: `PUT /campaigns/{id}/submit` (Status: PENDING_APPROVAL).
3.  **Admin Onayı**: Admin token'ı ile `PUT /admin/campaigns/{id}/approve` (Status: ACTIVE).

---

### Adım 3: Bağış ve Makbuz (Ana Test)

**Ön Hazırlık:** Aktif bir kampanya ID'si gereklidir.
*   **Mevcut Aktif Kampanya:** `faf27b19-0607-4434-8326-e36528765e77` (İhtiyaç Sahibi Ailelere Yardım)

#### 3.1. Bağış Kaydı Oluşturma
*   **Endpoint:** `POST /api/v1/donations`
*   **Rol:** Donor veya Anonim
*   **Body:**
    ```json
    {
      "campaignId": "faf27b19-0607-4434-8326-e36528765e77",
      "amount": 1000,
      "currency": "TRY",
      "isAnonymous": false,
      "donorDisplayName": "Hayirsever",
      "donorMessage": "Destek olmak istedim."
    }
    ```
*   **Sonuç:** Response içindeki **UUID**'yi (Donation ID) kopyalayın.

#### 3.2. Ödeme Yapma (Mock)
*   **Endpoint:** `POST /api/v1/payments/direct`
*   **Body:**
    ```json
    {
      "donationId": "BURAYA_DONATION_UUID_YAPISTIR",
      "cardHolderName": "Ali Veli",
      "cardNumber": "4111111111111111",
      "expireMonth": "12",
      "expireYear": "2025",
      "cvc": "123",
      "saveCard": false
    }
    ```
*   **Sonuç:** `200 OK`. Response içindeki `receiptBarcode` alanını kopyalayın (örn: `SB-2025-XXXX`).

#### 3.3. Makbuz Doğrulama (Public)
*   **Endpoint:** `GET /api/v1/receipts/verify/{barcode}`
*   **Auth:** Gerekmez (Public Endpoint)
*   **Test:** Kopyaladığınız barkodu URL'e yapıştırın.
*   **Beklenen:** Bağışçı isminin maskelenmiş hali (örn: `A** V***`) ve doğru tutar görünmelidir.

---

## 🛠️ Sorun Giderme

Eğer 500 hatası alırsanız veya şema hatası ("column does not exist") görürseniz, son migration'ın uygulandığından emin olun:
`docker-compose up -d --build backend`
