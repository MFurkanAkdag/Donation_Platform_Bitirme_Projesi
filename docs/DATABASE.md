# Şeffaf Bağış Platformu Veritabanı

PostgreSQL 15 üzerine kuruludur ve Flyway ile yönetilir. UUID üretimi için `pgcrypto` uzantısı aktif edilir. Şema 5NF prensiplerine göre modüler parçalara ayrıldı.

## Ana Modüller
- **Enum tipleri:** user_role, user_status, organization_type, verification_status, campaign_status, donation_type_enum, donation_status, payment_method, evidence_type, evidence_status, application_status, notification_type.
- **Kullanıcı ve güvenlik:** users, user_profiles, user_sensitive_data, user_preferences, refresh_tokens, password_reset_tokens.
- **Organizasyon:** organizations, organization_contacts, organization_documents, organization_bank_accounts, transparency_scores, transparency_score_history, favorite_organizations.
- **Kategoriler ve bağış türleri:** categories (hiyerarşik), donation_types (fıkhi kurallar dahil).
- **Kampanya:** campaigns, campaign_categories, campaign_donation_types, campaign_updates, campaign_images, campaign_followers.
- **Bağış ve ödeme:** donations, transactions, donation_receipts, recurring_donations, bank_transfer_references.
- **Kanıt ve şeffaflık:** evidences, evidence_documents, transparency_scores, transparency_score_history.
- **Başvurular:** applications, application_documents.
- **Bildirim / log / rapor:** notifications, audit_logs, email_logs, reports.
- **Sistem ayarları:** system_settings (public/private ayrımı ve tip bilgisi).
- **Görünümler:** v_active_campaigns, v_organization_summary kampanya ve kurum özetleri için.
- **Tetikler:** `update_updated_at_column` fonksiyonu; users, user_profiles, user_sensitive_data, organizations, campaigns, donations, transparency_scores, applications tablolarında `updated_at` sütununu otomatik günceller.

## Migrasyon Akışı (Flyway)
- **V1**: `pgcrypto` uzantısı ve tüm ENUM tipleri.
- **V2**: Kullanıcı çekirdek tabloları (users, profiller, hassas veri, tercihler).
- **V3**: Organizasyon taban tabloları ve banka/iletişim/doküman alt tabloları.
- **V4**: Kategoriler ve donation_types.
- **V5**: Kampanyalar ve ilişkileri (kategori/bağış türü, güncelleme, galeri, takipçiler).
- **V6**: Bağış/ödeme akışı (donations, transactions, receipts, recurring_donations, bank_transfer_references).
- **V7**: Kanıt ve şeffaflık tabloları.
- **V8**: Yardım başvuruları ve ekleri.
- **V9**: Bildirim, audit, e-posta logları ve raporlar.
- **V10**: Refresh ve password reset token tabloları.
- **V11**: Sistem ayarları, favori kurumlar ve chat oturum/mesaj tabloları.
- **V12**: Performans indeksleri ve kısmi indeksler (örn. unread notifications, aktif tekrar ödemeler).
- **V13**: `updated_at` tetik fonksiyonu ve ilgili tetikleyiciler.
- **V14**: Raporlama görünümleri (aktif kampanya özeti, organizasyon özeti).
- **V15**: `system_settings` başlangıç verisi (platform adı, min/max bağış tutarı, komisyon, bakım modu vb.).

## Notlar
- Tüm yabancı anahtarlar ilgili tablolardan sonra tanımlanır; kritik ilişkilerde `ON DELETE CASCADE` kullanılır (ör. user_profiler, organization_* alt tabloları).
- İndeksler işlevsel alanlara göre dağıtıldı; kısmi indeksler (kampanyaların featured durumu, unread notifications, aktif recurring_donations) sorgu maliyetini düşürmek için kullanılır.
- Görünümler raporlama/özeti kolaylaştırır; hesaplanmış yüzde alanı `progress_percentage` olarak sağlanır.
