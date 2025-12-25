# Test Kullanıcıları - Credentials

Bu dosya Şeffaf Bağış Platformu'ndaki test kullanıcılarını ve şifrelerini içerir.

---

## 🔐 Frontend Demo Users (Login Sayfasında Gösterilen)

### Admin User
- **Email:** `admin@example.com`
- **Password:** `Admin123!`
- **Role:** ADMIN
- **Kullanım:** Admin paneli testleri

### Foundation User
- **Email:** `foundation@example.com`
- **Password:** `Foundation123!`
- **Role:** FOUNDATION
- **Kullanım:** Vakıf işlemleri testleri

---

## 🧪 Phase 18 Test Users (Manuel Oluşturuldu)

### 1. Foundation User (Vakıf)
- **Email:** `vakif@test.com`
- **Password:** `Test123!` *(tahmini)*
- **Role:** FOUNDATION
- **Organization:** "Hayır Vakfı" (APPROVED)
- **Kullanım:** Kampanya oluşturma/yönetme testleri

### 2. Admin User
- **Email:** `admin@test.com`
- **Password:** `Test123!` *(tahmini)*
- **Role:** ADMIN
- **Kullanım:** Onay işlemleri (org/campaign approve)

### 3. Donor User (Bağışçı)
- **Email:** `donor@test.com`
- **Password:** `Test123!` *(tahmini)*
- **Role:** DONOR
- **Bağış Geçmişi:** 1000 TRY bağış yapmış
- **Kullanım:** Bağış akışı testleri

### 4. Integration Test User (Frontend Auth)
- **Email:** `test_success_verification_01@example.com`
- **Password:** `Password123!`
- **Role:** DONOR
- **Status:** ✅ Login/Register entegrasyonu doğrulandı
- **Kullanım:** Frontend authentication testleri

---

## 📋 Aktif Kampanya Bilgisi

**Kampanya:** "İhtiyaç Sahibi Ailelere Yardım"
- **Campaign ID:** `faf27b19-0607-4434-8326-e36528765e77`
- **Status:** ACTIVE
- **Foundation:** Hayır Vakfı
- **Kullanım:** Bağış testleri için

---

## 🔑 Şifre Politikası

Tüm test kullanıcıları için standart şifre formatı:
- Minimum 8 karakter
- En az 1 büyük harf
- En az 1 küçük harf
- En az 1 rakam
- En az 1 özel karakter (!@#$%)

**Varsayılan şifreler:**
- `Test123!` - Eski test kullanıcıları
- `Admin123!` - Demo admin
- `Foundation123!` - Demo foundation
- `Password123!` - Yeni frontend testleri

---

## ⚠️ Güvenlik Notu

**UYARI:** Bu dosya sadece development/test ortamı içindir. Production'da asla bu şifreler kullanılmamalıdır!

**Dosya Yeri:** `docs/TEST_USERS.md`
