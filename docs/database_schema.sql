-- ============================================================================
-- ŞEFFAF BAĞIŞ PLATFORMU - VERİTABANI ŞEMASI
-- 5NF (Fifth Normal Form) Uyumlu Tasarım
-- PostgreSQL 15+
-- ============================================================================

-- Veritabanı oluşturma (production'da ayrıca çalıştırılacak)
-- CREATE DATABASE seffaf_bagis_db WITH ENCODING 'UTF8' LC_COLLATE 'tr_TR.UTF-8';

-- UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- BÖLÜM 1: ENUM TİPLERİ
-- ============================================================================

-- Kullanıcı rolleri
CREATE TYPE user_role AS ENUM ('donor', 'foundation', 'beneficiary', 'admin');

-- Kullanıcı durumu
CREATE TYPE user_status AS ENUM ('active', 'inactive', 'suspended', 'pending_verification');

-- Organizasyon türü
CREATE TYPE organization_type AS ENUM ('foundation', 'association', 'ngo');

-- Organizasyon doğrulama durumu
CREATE TYPE verification_status AS ENUM ('pending', 'in_review', 'approved', 'rejected');

-- Kampanya durumu
CREATE TYPE campaign_status AS ENUM ('draft', 'pending_approval', 'active', 'paused', 'completed', 'cancelled');

-- Bağış türü (dini/sosyal)
CREATE TYPE donation_type_enum AS ENUM ('zekat', 'fitre', 'sadaka', 'kurban', 'genel', 'afet');

-- Bağış durumu
CREATE TYPE donation_status AS ENUM ('pending', 'completed', 'failed', 'refunded');

-- Ödeme yöntemi
CREATE TYPE payment_method AS ENUM ('credit_card', 'bank_transfer', 'mobile_payment');

-- Kanıt türü
CREATE TYPE evidence_type AS ENUM ('invoice', 'receipt', 'photo', 'video', 'delivery_report', 'other');

-- Kanıt durumu
CREATE TYPE evidence_status AS ENUM ('pending', 'approved', 'rejected');

-- Başvuru durumu
CREATE TYPE application_status AS ENUM ('pending', 'in_review', 'approved', 'rejected', 'completed');

-- Bildirim türü
CREATE TYPE notification_type AS ENUM ('donation_received', 'campaign_update', 'evidence_required', 'score_change', 'system');


-- ============================================================================
-- BÖLÜM 2: TEMEL TABLOLAR (Core Tables)
-- ============================================================================

-- ---------------------------------------------------------------------------
-- TABLO: users
-- Açıklama: Temel kullanıcı bilgileri (tüm roller için)
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'donor',
    status user_status NOT NULL DEFAULT 'pending_verification',
    email_verified BOOLEAN DEFAULT FALSE,
    email_verified_at TIMESTAMPTZ,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);


-- ---------------------------------------------------------------------------
-- TABLO: user_profiles
-- Açıklama: Kullanıcı profil bilgileri (1:1 ilişki, ayrı tablo - 2NF)
-- ---------------------------------------------------------------------------
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    display_name VARCHAR(100),
    avatar_url VARCHAR(500),
    bio TEXT,
    preferred_language VARCHAR(5) DEFAULT 'tr',
    timezone VARCHAR(50) DEFAULT 'Europe/Istanbul',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);


-- ---------------------------------------------------------------------------
-- TABLO: user_sensitive_data
-- Açıklama: KVKK kapsamında şifrelenecek hassas veriler (ayrı tablo - güvenlik)
-- NOT: Bu tablodaki veriler AES-256 ile şifrelenecek
-- ---------------------------------------------------------------------------
CREATE TABLE user_sensitive_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tc_kimlik_encrypted BYTEA,              -- AES-256 şifreli
    phone_encrypted BYTEA,                   -- AES-256 şifreli
    address_encrypted BYTEA,                 -- AES-256 şifreli
    birth_date_encrypted BYTEA,              -- AES-256 şifreli
    data_processing_consent BOOLEAN DEFAULT FALSE,
    consent_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_sensitive_user_id ON user_sensitive_data(user_id);


-- ---------------------------------------------------------------------------
-- TABLO: user_preferences
-- Açıklama: Kullanıcı tercihleri (bildirim, gizlilik ayarları)
-- ---------------------------------------------------------------------------
CREATE TABLE user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    email_notifications BOOLEAN DEFAULT TRUE,
    sms_notifications BOOLEAN DEFAULT FALSE,
    donation_visibility VARCHAR(20) DEFAULT 'anonymous', -- 'public', 'anonymous', 'private'
    show_in_donor_list BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================================
-- BÖLÜM 3: ORGANİZASYON TABLOLARI
-- ============================================================================

-- ---------------------------------------------------------------------------
-- TABLO: organizations
-- Açıklama: Vakıf ve dernek temel bilgileri
-- ---------------------------------------------------------------------------
CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    organization_type organization_type NOT NULL,
    legal_name VARCHAR(255) NOT NULL,
    trade_name VARCHAR(255),
    tax_number VARCHAR(20) UNIQUE,
    derbis_number VARCHAR(50),                -- Dernekler için DERBİS no
    mersis_number VARCHAR(50),                -- Vakıflar için MERSİS no
    establishment_date DATE,
    description TEXT,
    mission_statement TEXT,
    logo_url VARCHAR(500),
    website_url VARCHAR(500),
    verification_status verification_status DEFAULT 'pending',
    verified_at TIMESTAMPTZ,
    verified_by UUID REFERENCES users(id),
    is_featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_organizations_user_id ON organizations(user_id);
CREATE INDEX idx_organizations_verification ON organizations(verification_status);
CREATE INDEX idx_organizations_type ON organizations(organization_type);


-- ---------------------------------------------------------------------------
-- TABLO: organization_contacts
-- Açıklama: Organizasyon iletişim bilgileri (1:N - birden fazla iletişim)
-- ---------------------------------------------------------------------------
CREATE TABLE organization_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    contact_type VARCHAR(50) NOT NULL,        -- 'primary', 'support', 'press'
    contact_name VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(20),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    district VARCHAR(100),
    postal_code VARCHAR(10),
    country VARCHAR(100) DEFAULT 'Türkiye',
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_org_contacts_org_id ON organization_contacts(organization_id);


-- ---------------------------------------------------------------------------
-- TABLO: organization_documents
-- Açıklama: Organizasyon belgeleri (vergi levhası, yetki belgesi vb.)
-- ---------------------------------------------------------------------------
CREATE TABLE organization_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    document_type VARCHAR(100) NOT NULL,      -- 'tax_certificate', 'authorization', 'derbis_record'
    document_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size INTEGER,
    mime_type VARCHAR(100),
    is_verified BOOLEAN DEFAULT FALSE,
    verified_at TIMESTAMPTZ,
    verified_by UUID REFERENCES users(id),
    expires_at DATE,
    uploaded_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_org_docs_org_id ON organization_documents(organization_id);
CREATE INDEX idx_org_docs_type ON organization_documents(document_type);


-- ---------------------------------------------------------------------------
-- TABLO: organization_bank_accounts
-- Açıklama: Organizasyon banka hesapları
-- ---------------------------------------------------------------------------
CREATE TABLE organization_bank_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    bank_name VARCHAR(100) NOT NULL,
    branch_name VARCHAR(100),
    account_holder VARCHAR(255) NOT NULL,
    iban VARCHAR(34) NOT NULL,
    currency VARCHAR(3) DEFAULT 'TRY',
    is_primary BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_org_bank_org_id ON organization_bank_accounts(organization_id);


-- ============================================================================
-- BÖLÜM 4: KATEGORİ VE BAĞIŞ TÜRLERİ
-- ============================================================================

-- ---------------------------------------------------------------------------
-- TABLO: categories
-- Açıklama: Kampanya kategorileri (Eğitim, Sağlık, Gıda vb.)
-- ---------------------------------------------------------------------------
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    icon_name VARCHAR(50),
    color_code VARCHAR(7),                    -- HEX color
    parent_id UUID REFERENCES categories(id), -- Alt kategoriler için
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_categories_parent ON categories(parent_id);


-- ---------------------------------------------------------------------------
-- TABLO: donation_types
-- Açıklama: Bağış türleri ve dini/sosyal kuralları
-- ---------------------------------------------------------------------------
CREATE TABLE donation_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type_code donation_type_enum UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    description TEXT,
    rules TEXT,                               -- Fıkhi kurallar (zekat nisabı vb.)
    minimum_amount DECIMAL(12,2),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================================
-- BÖLÜM 5: KAMPANYA TABLOLARI
-- ============================================================================

-- ---------------------------------------------------------------------------
-- TABLO: campaigns
-- Açıklama: Bağış kampanyaları
-- ---------------------------------------------------------------------------
CREATE TABLE campaigns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    summary VARCHAR(500),
    description TEXT NOT NULL,
    cover_image_url VARCHAR(500),
    target_amount DECIMAL(12,2) NOT NULL,
    collected_amount DECIMAL(12,2) DEFAULT 0,
    donor_count INTEGER DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'TRY',
    status campaign_status DEFAULT 'draft',
    start_date TIMESTAMPTZ,
    end_date TIMESTAMPTZ,
    evidence_deadline_days INTEGER DEFAULT 15, -- Kanıt yükleme süresi
    is_urgent BOOLEAN DEFAULT FALSE,
    is_featured BOOLEAN DEFAULT FALSE,
    location_city VARCHAR(100),
    location_district VARCHAR(100),
    beneficiary_count INTEGER,                -- Kaç kişiye ulaşılacak
    created_by UUID REFERENCES users(id),
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_campaigns_org_id ON campaigns(organization_id);
CREATE INDEX idx_campaigns_status ON campaigns(status);
CREATE INDEX idx_campaigns_slug ON campaigns(slug);
CREATE INDEX idx_campaigns_dates ON campaigns(start_date, end_date);
CREATE INDEX idx_campaigns_featured ON campaigns(is_featured) WHERE is_featured = TRUE;


-- ---------------------------------------------------------------------------
-- TABLO: campaign_categories (Many-to-Many)
-- Açıklama: Kampanya-Kategori ilişkisi
-- ---------------------------------------------------------------------------
CREATE TABLE campaign_categories (
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (campaign_id, category_id)
);

CREATE INDEX idx_campaign_categories_campaign ON campaign_categories(campaign_id);
CREATE INDEX idx_campaign_categories_category ON campaign_categories(category_id);


-- ---------------------------------------------------------------------------
-- TABLO: campaign_donation_types (Many-to-Many)
-- Açıklama: Kampanyanın kabul ettiği bağış türleri
-- ---------------------------------------------------------------------------
CREATE TABLE campaign_donation_types (
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    donation_type_id UUID NOT NULL REFERENCES donation_types(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (campaign_id, donation_type_id)
);


-- ---------------------------------------------------------------------------
-- TABLO: campaign_updates
-- Açıklama: Kampanya güncellemeleri/duyuruları
-- ---------------------------------------------------------------------------
CREATE TABLE campaign_updates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(500),
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_campaign_updates_campaign ON campaign_updates(campaign_id);


-- ---------------------------------------------------------------------------
-- TABLO: campaign_images
-- Açıklama: Kampanya görselleri (galeri)
-- ---------------------------------------------------------------------------
CREATE TABLE campaign_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    caption VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_campaign_images_campaign ON campaign_images(campaign_id);


-- ============================================================================
-- BÖLÜM 6: BAĞIŞ VE ÖDEME TABLOLARI
-- ============================================================================

-- ---------------------------------------------------------------------------
-- TABLO: donations
-- Açıklama: Bağış kayıtları
-- ---------------------------------------------------------------------------
CREATE TABLE donations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaigns(id),
    donor_id UUID REFERENCES users(id),       -- NULL olabilir (anonim)
    donation_type_id UUID REFERENCES donation_types(id),
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'TRY',
    status donation_status DEFAULT 'pending',
    is_anonymous BOOLEAN DEFAULT FALSE,
    donor_message VARCHAR(500),               -- Bağışçı notu
    donor_display_name VARCHAR(100),          -- Anonim değilse gösterilecek isim
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_donations_campaign ON donations(campaign_id);
CREATE INDEX idx_donations_donor ON donations(donor_id);
CREATE INDEX idx_donations_status ON donations(status);
CREATE INDEX idx_donations_created ON donations(created_at);


-- ---------------------------------------------------------------------------
-- TABLO: transactions
-- Açıklama: Ödeme işlemleri (Iyzico/PayTR entegrasyonu)
-- ---------------------------------------------------------------------------
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donation_id UUID NOT NULL REFERENCES donations(id),
    payment_method payment_method NOT NULL,
    payment_provider VARCHAR(50) NOT NULL,    -- 'iyzico', 'paytr'
    provider_transaction_id VARCHAR(255),     -- Ödeme sağlayıcı işlem ID
    provider_payment_id VARCHAR(255),
    amount DECIMAL(12,2) NOT NULL,
    fee_amount DECIMAL(12,2) DEFAULT 0,       -- Komisyon
    net_amount DECIMAL(12,2),                 -- Net tutar
    currency VARCHAR(3) DEFAULT 'TRY',
    status VARCHAR(50) NOT NULL,              -- Provider'dan gelen durum
    error_code VARCHAR(50),
    error_message TEXT,
    card_last_four VARCHAR(4),
    card_brand VARCHAR(50),                   -- 'visa', 'mastercard'
    is_3d_secure BOOLEAN DEFAULT FALSE,
    raw_response JSONB,                       -- Provider'dan gelen ham yanıt
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_donation ON transactions(donation_id);
CREATE INDEX idx_transactions_provider_id ON transactions(provider_transaction_id);
CREATE INDEX idx_transactions_status ON transactions(status);


-- ---------------------------------------------------------------------------
-- TABLO: donation_receipts
-- Açıklama: Dijital bağış makbuzları
-- ---------------------------------------------------------------------------
CREATE TABLE donation_receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donation_id UUID UNIQUE NOT NULL REFERENCES donations(id),
    receipt_number VARCHAR(50) UNIQUE NOT NULL,
    receipt_url VARCHAR(500),
    issued_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================================
-- BÖLÜM 7: KANIT VE ŞEFFAFLIK TABLOLARI
-- ============================================================================

-- ---------------------------------------------------------------------------
-- TABLO: evidences
-- Açıklama: Harcama kanıtları
-- ---------------------------------------------------------------------------
CREATE TABLE evidences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaigns(id),
    evidence_type evidence_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    amount_spent DECIMAL(12,2),               -- Harcanan tutar
    vendor_name VARCHAR(255),                 -- Satıcı/tedarikçi adı
    vendor_tax_number VARCHAR(20),
    status evidence_status DEFAULT 'pending',
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    rejection_reason TEXT,
    uploaded_by UUID REFERENCES users(id),
    uploaded_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_evidences_campaign ON evidences(campaign_id);
CREATE INDEX idx_evidences_status ON evidences(status);


-- ---------------------------------------------------------------------------
-- TABLO: evidence_documents
-- Açıklama: Kanıt dosyaları (bir kanıta birden fazla dosya)
-- ---------------------------------------------------------------------------
CREATE TABLE evidence_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    evidence_id UUID NOT NULL REFERENCES evidences(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size INTEGER,
    mime_type VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    uploaded_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_evidence_docs_evidence ON evidence_documents(evidence_id);


-- ---------------------------------------------------------------------------
-- TABLO: transparency_scores
-- Açıklama: Organizasyon şeffaflık skorları (güncel durum)
-- ---------------------------------------------------------------------------
CREATE TABLE transparency_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID UNIQUE NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    current_score DECIMAL(5,2) DEFAULT 50.00, -- 0-100 arası
    total_campaigns INTEGER DEFAULT 0,
    completed_campaigns INTEGER DEFAULT 0,
    on_time_reports INTEGER DEFAULT 0,        -- Zamanında yapılan raporlar
    late_reports INTEGER DEFAULT 0,           -- Geç yapılan raporlar
    approved_evidences INTEGER DEFAULT 0,
    rejected_evidences INTEGER DEFAULT 0,
    last_calculated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transparency_org ON transparency_scores(organization_id);
CREATE INDEX idx_transparency_score ON transparency_scores(current_score);


-- ---------------------------------------------------------------------------
-- TABLO: transparency_score_history
-- Açıklama: Skor değişim geçmişi (audit trail)
-- ---------------------------------------------------------------------------
CREATE TABLE transparency_score_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    previous_score DECIMAL(5,2),
    new_score DECIMAL(5,2) NOT NULL,
    change_reason VARCHAR(255) NOT NULL,      -- 'evidence_approved', 'late_report', etc.
    campaign_id UUID REFERENCES campaigns(id),
    evidence_id UUID REFERENCES evidences(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_score_history_org ON transparency_score_history(organization_id);
CREATE INDEX idx_score_history_created ON transparency_score_history(created_at);


-- ============================================================================
-- BÖLÜM 8: BAŞVURU VE FAYDALANCI TABLOLARI
-- ============================================================================

-- ---------------------------------------------------------------------------
-- TABLO: applications
-- Açıklama: Yardım başvuruları
-- ---------------------------------------------------------------------------
CREATE TABLE applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    applicant_id UUID REFERENCES users(id),
    category_id UUID REFERENCES categories(id),
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    requested_amount DECIMAL(12,2),
    status application_status DEFAULT 'pending',
    location_city VARCHAR(100),
    location_district VARCHAR(100),
    household_size INTEGER,                   -- Hane halkı sayısı
    urgency_level INTEGER DEFAULT 1,          -- 1-5 arası
    assigned_organization_id UUID REFERENCES organizations(id),
    assigned_campaign_id UUID REFERENCES campaigns(id),
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_applications_applicant ON applications(applicant_id);
CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_applications_category ON applications(category_id);


-- ---------------------------------------------------------------------------
-- TABLO: application_documents
-- Açıklama: Başvuru belgeleri
-- ---------------------------------------------------------------------------
CREATE TABLE application_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    document_type VARCHAR(100) NOT NULL,      -- 'id_card', 'income_proof', 'medical_report'
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    uploaded_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_app_docs_application ON application_documents(application_id);


-- ============================================================================
-- BÖLÜM 9: BİLDİRİM VE LOG TABLOLARI
-- ============================================================================

-- ---------------------------------------------------------------------------
-- TABLO: notifications
-- Açıklama: Kullanıcı bildirimleri
-- ---------------------------------------------------------------------------
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type notification_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    data JSONB,                               -- Ek veri (campaign_id, donation_id vb.)
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;


-- ---------------------------------------------------------------------------
-- TABLO: audit_logs
-- Açıklama: Sistem denetim kayıtları (KVKK uyumu)
-- ---------------------------------------------------------------------------
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,             -- 'user.login', 'donation.create', 'evidence.approve'
    entity_type VARCHAR(100),                 -- 'user', 'campaign', 'donation'
    entity_id UUID,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);


-- ---------------------------------------------------------------------------
-- TABLO: email_logs
-- Açıklama: Gönderilen e-posta kayıtları
-- ---------------------------------------------------------------------------
CREATE TABLE email_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    email_to VARCHAR(255) NOT NULL,
    email_type VARCHAR(100) NOT NULL,         -- 'welcome', 'donation_receipt', 'evidence_reminder'
    subject VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'sent',        -- 'sent', 'failed', 'bounced'
    provider_message_id VARCHAR(255),
    error_message TEXT,
    sent_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_logs_user ON email_logs(user_id);
CREATE INDEX idx_email_logs_type ON email_logs(email_type);


-- ============================================================================
-- BÖLÜM 10: AI/NLP TABLOLARI
-- ============================================================================

-- ---------------------------------------------------------------------------
-- TABLO: chat_sessions
-- Açıklama: AI chatbot oturum kayıtları
-- ---------------------------------------------------------------------------
CREATE TABLE chat_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    session_token VARCHAR(255) UNIQUE NOT NULL,
    started_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMPTZ,
    message_count INTEGER DEFAULT 0
);

CREATE INDEX idx_chat_sessions_user ON chat_sessions(user_id);
CREATE INDEX idx_chat_sessions_token ON chat_sessions(session_token);


-- ---------------------------------------------------------------------------
-- TABLO: chat_messages
-- Açıklama: Chat mesajları ve intent analizi
-- ---------------------------------------------------------------------------
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,                -- 'user', 'assistant'
    content TEXT NOT NULL,
    detected_intent VARCHAR(100),             -- 'donate_zekat', 'ask_campaign', 'request_help'
    intent_confidence DECIMAL(5,4),           -- 0.0000 - 1.0000
    detected_entities JSONB,                  -- {amount: 500, category: 'education', city: 'Istanbul'}
    suggested_campaign_id UUID REFERENCES campaigns(id),
    response_time_ms INTEGER,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_messages_session ON chat_messages(session_id);
CREATE INDEX idx_chat_messages_intent ON chat_messages(detected_intent);


-- ============================================================================
-- BÖLÜM 11: EK TABLOLAR (Eksik Tespitler Sonrası Eklenen)
-- ============================================================================

-- ---------------------------------------------------------------------------
-- TABLO: recurring_donations
-- Açıklama: Tekrarlayan (aylık/haftalık) bağış abonelikleri
-- ---------------------------------------------------------------------------
CREATE TABLE recurring_donations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donor_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    campaign_id UUID REFERENCES campaigns(id),           -- NULL ise genel vakıf bağışı
    organization_id UUID REFERENCES organizations(id),   -- Kampanya yoksa direkt vakfa
    donation_type_id UUID REFERENCES donation_types(id),
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'TRY',
    frequency VARCHAR(20) NOT NULL,                      -- 'weekly', 'monthly', 'yearly'
    next_payment_date DATE NOT NULL,
    last_payment_date DATE,
    total_donated DECIMAL(12,2) DEFAULT 0,
    payment_count INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'active',                 -- 'active', 'paused', 'cancelled'
    card_token VARCHAR(255),                             -- Kaydedilmiş kart token (Iyzico)
    failure_count INTEGER DEFAULT 0,                     -- Ardışık başarısız deneme
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recurring_donor ON recurring_donations(donor_id);
CREATE INDEX idx_recurring_next_payment ON recurring_donations(next_payment_date) WHERE status = 'active';


-- ---------------------------------------------------------------------------
-- TABLO: campaign_followers
-- Açıklama: Kullanıcıların takip ettiği kampanyalar
-- ---------------------------------------------------------------------------
CREATE TABLE campaign_followers (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    notify_on_update BOOLEAN DEFAULT TRUE,
    notify_on_complete BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, campaign_id)
);

CREATE INDEX idx_campaign_followers_campaign ON campaign_followers(campaign_id);


-- ---------------------------------------------------------------------------
-- TABLO: favorite_organizations
-- Açıklama: Kullanıcıların favori vakıfları
-- ---------------------------------------------------------------------------
CREATE TABLE favorite_organizations (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, organization_id)
);

CREATE INDEX idx_favorite_orgs_org ON favorite_organizations(organization_id);


-- ---------------------------------------------------------------------------
-- TABLO: reports
-- Açıklama: Şikayet ve dolandırıcılık bildirimleri
-- ---------------------------------------------------------------------------
CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID REFERENCES users(id),               -- NULL ise anonim
    report_type VARCHAR(50) NOT NULL,                    -- 'fraud', 'inappropriate', 'spam', 'other'
    entity_type VARCHAR(50) NOT NULL,                    -- 'campaign', 'organization', 'user'
    entity_id UUID NOT NULL,
    reason VARCHAR(255) NOT NULL,
    description TEXT,
    evidence_urls TEXT[],                                -- Kanıt linkleri
    status VARCHAR(20) DEFAULT 'pending',                -- 'pending', 'investigating', 'resolved', 'dismissed'
    resolution_notes TEXT,
    resolved_by UUID REFERENCES users(id),
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reports_entity ON reports(entity_type, entity_id);
CREATE INDEX idx_reports_status ON reports(status);


-- ---------------------------------------------------------------------------
-- TABLO: bank_transfer_references
-- Açıklama: Havale/EFT bağış eşleştirme referansları
-- ---------------------------------------------------------------------------
CREATE TABLE bank_transfer_references (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_code VARCHAR(20) UNIQUE NOT NULL,          -- Benzersiz referans kodu
    campaign_id UUID REFERENCES campaigns(id),
    organization_id UUID REFERENCES organizations(id),
    donor_id UUID REFERENCES users(id),
    expected_amount DECIMAL(12,2),
    donation_type_id UUID REFERENCES donation_types(id),
    status VARCHAR(20) DEFAULT 'pending',                -- 'pending', 'matched', 'expired'
    matched_donation_id UUID REFERENCES donations(id),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bank_ref_code ON bank_transfer_references(reference_code);
CREATE INDEX idx_bank_ref_status ON bank_transfer_references(status) WHERE status = 'pending';


-- ---------------------------------------------------------------------------
-- TABLO: system_settings
-- Açıklama: Platform geneli sistem ayarları
-- ---------------------------------------------------------------------------
CREATE TABLE system_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT NOT NULL,
    value_type VARCHAR(20) DEFAULT 'string',             -- 'string', 'number', 'boolean', 'json'
    description VARCHAR(255),
    is_public BOOLEAN DEFAULT FALSE,                     -- Frontend'e gönderilsin mi
    updated_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Varsayılan sistem ayarları
INSERT INTO system_settings (setting_key, setting_value, value_type, description, is_public) VALUES
('platform_name', 'Şeffaf Bağış Platformu', 'string', 'Platform adı', TRUE),
('min_donation_amount', '10', 'number', 'Minimum bağış tutarı (TRY)', TRUE),
('max_donation_amount', '1000000', 'number', 'Maximum bağış tutarı (TRY)', TRUE),
('evidence_deadline_days', '15', 'number', 'Varsayılan kanıt yükleme süresi (gün)', FALSE),
('transparency_score_threshold', '40', 'number', 'Kampanya açma için minimum skor', FALSE),
('commission_rate', '0', 'number', 'Platform komisyon oranı (%)', FALSE),
('maintenance_mode', 'false', 'boolean', 'Bakım modu aktif mi', TRUE);


-- ============================================================================
-- BÖLÜM 12: OTURUM VE TOKEN TABLOLARI
-- ============================================================================

-- ---------------------------------------------------------------------------
-- TABLO: refresh_tokens
-- Açıklama: JWT refresh token'ları
-- ---------------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    device_info VARCHAR(255),
    ip_address INET,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);


-- ---------------------------------------------------------------------------
-- TABLO: password_reset_tokens
-- Açıklama: Şifre sıfırlama token'ları
-- ---------------------------------------------------------------------------
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_password_reset_user ON password_reset_tokens(user_id);


-- ============================================================================
-- BÖLÜM 12: TRIGGER FONKSİYONLARI
-- ============================================================================

-- updated_at otomatik güncelleme
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger'ları uygula
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_profiles_updated_at BEFORE UPDATE ON user_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_sensitive_updated_at BEFORE UPDATE ON user_sensitive_data
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_organizations_updated_at BEFORE UPDATE ON organizations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_campaigns_updated_at BEFORE UPDATE ON campaigns
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_donations_updated_at BEFORE UPDATE ON donations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transparency_scores_updated_at BEFORE UPDATE ON transparency_scores
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_applications_updated_at BEFORE UPDATE ON applications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();


-- ============================================================================
-- BÖLÜM 13: GÖRÜNÜMLER (VIEWS)
-- ============================================================================

-- Aktif kampanyalar özet görünümü
CREATE VIEW v_active_campaigns AS
SELECT 
    c.id,
    c.title,
    c.slug,
    c.target_amount,
    c.collected_amount,
    c.donor_count,
    c.status,
    c.end_date,
    o.legal_name AS organization_name,
    o.logo_url AS organization_logo,
    ts.current_score AS transparency_score,
    ROUND((c.collected_amount / c.target_amount * 100), 2) AS progress_percentage
FROM campaigns c
JOIN organizations o ON c.organization_id = o.id
LEFT JOIN transparency_scores ts ON o.id = ts.organization_id
WHERE c.status = 'active'
ORDER BY c.is_featured DESC, c.created_at DESC;


-- Organizasyon özet görünümü
CREATE VIEW v_organization_summary AS
SELECT 
    o.id,
    o.legal_name,
    o.organization_type,
    o.verification_status,
    o.logo_url,
    ts.current_score AS transparency_score,
    COUNT(DISTINCT c.id) AS total_campaigns,
    COUNT(DISTINCT CASE WHEN c.status = 'active' THEN c.id END) AS active_campaigns,
    COALESCE(SUM(c.collected_amount), 0) AS total_collected
FROM organizations o
LEFT JOIN transparency_scores ts ON o.id = ts.organization_id
LEFT JOIN campaigns c ON o.id = c.organization_id
WHERE o.verification_status = 'approved'
GROUP BY o.id, ts.current_score;


-- ============================================================================
-- TABLO İLİŞKİ ÖZETİ
-- ============================================================================
/*
users (1) ─────────────── (1) user_profiles
      │                         
      ├── (1) ──────────── (1) user_sensitive_data
      │                         
      ├── (1) ──────────── (1) user_preferences
      │                         
      ├── (1) ──────────── (1) organizations ──┬── (N) organization_contacts
      │                                        ├── (N) organization_documents
      │                                        ├── (N) organization_bank_accounts
      │                                        ├── (N) campaigns
      │                                        └── (1) transparency_scores
      │
      ├── (N) ──────────── donations ──────────── (1) transactions
      │                       │                        │
      │                       └── (1) donation_receipts
      │
      ├── (N) ──────────── applications ───────── (N) application_documents
      │
      ├── (N) ──────────── notifications
      │
      ├── (N) ──────────── audit_logs
      │
      └── (N) ──────────── chat_sessions ──────── (N) chat_messages

campaigns (N) ─────────── (M) categories      [via campaign_categories]
          │
          ├── (N) ─────── (M) donation_types  [via campaign_donation_types]
          │
          ├── (N) ─────── campaign_updates
          │
          ├── (N) ─────── campaign_images
          │
          └── (N) ─────── evidences ──────────── (N) evidence_documents
*/
