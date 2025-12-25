# Swagger UI - Manuel Test Sonuçları

Bu dosya, `SWAGGER_UI_TEST_PHASES.md` planındaki adımların gerçek uygulama sonuçlarını içerir.

---

## 🎭 Senaryo 1: Kullanıcı Rolleri ve Hazırlık

### 1. Admin Kullanıcısı Kaydı
*   **İşlem:** `POST /api/v1/auth/register`
*   **Durum:** ✅ Başarılı (201 Created)
*   **Detaylar:** Admin kullanıcısı (ilk etapta DONOR rolüyle) sisteme kaydedildi.
*   **Response:**
    ```json
    {
      "success": true,
      "message": "Kayıt başarılı. Lütfen e-posta adresinizi doğrulayın.",
      "data": {
        "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
        "user": {
          "id": "fa30fd75-fddc-41f5-b519-9d735900df62",
          "email": "admin@seffafbagis.com",
          "role": "DONOR",
          "fullName": "Sistem Admin"
        }
      }
    }
    ```
*   **Sonraki Adım:** Bu kullanıcının rolünü veritabanından `ADMIN` olarak güncellemek gerekiyor.

### 2. Admin Yetkisi Verme
*   **İşlem:** Veritabanı (Docker PSQL) üzerinden güncelleme.
*   **Komut:** `UPDATE users SET role = 'ADMIN' WHERE email = 'admin@seffafbagis.com';`
*   **Durum:** ✅ Başarılı (1 satır güncellendi).

### 3. Admin Girişi (Login)
*   **İşlem:** `POST /api/v1/auth/login`
*   **Durum:** ✅ Başarılı (200 OK)
*   **Önemli Not:** Response içinde kullanıcının rolünün `ADMIN` olduğu teyit edildi.
*   **Response:**
    ```json
    {
      "success": true,
      "message": "Giriş başarılı",
      "data": {
        "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
        "tokenType": "Bearer",
        "user": {
          "id": "fa30fd75-fddc-41f5-b519-9d735900df62",
          "email": "admin@seffafbagis.com",
          "role": "ADMIN", // ROL GÜNCELLENDİ
          "fullName": "Sistem Admin"
        }
      }
    }
    ```

---
