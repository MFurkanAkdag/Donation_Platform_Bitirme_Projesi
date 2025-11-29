-- Organization tables
CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    organization_type organization_type NOT NULL,
    legal_name VARCHAR(255) NOT NULL,
    trade_name VARCHAR(255),
    tax_number VARCHAR(20) UNIQUE,
    derbis_number VARCHAR(50),
    mersis_number VARCHAR(50),
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

CREATE TABLE organization_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    contact_type VARCHAR(50) NOT NULL,
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

CREATE TABLE organization_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    document_type VARCHAR(100) NOT NULL,
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
