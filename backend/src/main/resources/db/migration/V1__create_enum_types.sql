-- Extensions and enum definitions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE user_role AS ENUM ('donor', 'foundation', 'beneficiary', 'admin');
CREATE TYPE user_status AS ENUM ('active', 'inactive', 'suspended', 'pending_verification');
CREATE TYPE organization_type AS ENUM ('foundation', 'association', 'ngo');
CREATE TYPE verification_status AS ENUM ('pending', 'in_review', 'approved', 'rejected');
CREATE TYPE campaign_status AS ENUM ('draft', 'pending_approval', 'active', 'paused', 'completed', 'cancelled');
CREATE TYPE donation_type_enum AS ENUM ('zekat', 'fitre', 'sadaka', 'kurban', 'genel', 'afet');
CREATE TYPE donation_status AS ENUM ('pending', 'completed', 'failed', 'refunded');
CREATE TYPE payment_method AS ENUM ('credit_card', 'bank_transfer', 'mobile_payment');
CREATE TYPE evidence_type AS ENUM ('invoice', 'receipt', 'photo', 'video', 'delivery_report', 'other');
CREATE TYPE evidence_status AS ENUM ('pending', 'approved', 'rejected');
CREATE TYPE application_status AS ENUM ('pending', 'in_review', 'approved', 'rejected', 'completed');
CREATE TYPE notification_type AS ENUM ('donation_received', 'campaign_update', 'evidence_required', 'score_change', 'system');
