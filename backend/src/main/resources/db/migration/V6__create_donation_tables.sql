-- Donation and payment tables
CREATE TABLE donations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaigns(id),
    donor_id UUID REFERENCES users(id),
    donation_type_id UUID REFERENCES donation_types(id),
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'TRY',
    status donation_status DEFAULT 'pending',
    is_anonymous BOOLEAN DEFAULT FALSE,
    donor_message VARCHAR(500),
    donor_display_name VARCHAR(100),
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donation_id UUID NOT NULL REFERENCES donations(id),
    payment_method payment_method NOT NULL,
    payment_provider VARCHAR(50) NOT NULL,
    provider_transaction_id VARCHAR(255),
    provider_payment_id VARCHAR(255),
    amount DECIMAL(12,2) NOT NULL,
    fee_amount DECIMAL(12,2) DEFAULT 0,
    net_amount DECIMAL(12,2),
    currency VARCHAR(3) DEFAULT 'TRY',
    status VARCHAR(50) NOT NULL,
    error_code VARCHAR(50),
    error_message TEXT,
    card_last_four VARCHAR(4),
    card_brand VARCHAR(50),
    is_3d_secure BOOLEAN DEFAULT FALSE,
    raw_response JSONB,
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE donation_receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donation_id UUID UNIQUE NOT NULL REFERENCES donations(id),
    receipt_number VARCHAR(50) UNIQUE NOT NULL,
    receipt_url VARCHAR(500),
    issued_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE recurring_donations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donor_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    campaign_id UUID REFERENCES campaigns(id),
    organization_id UUID REFERENCES organizations(id),
    donation_type_id UUID REFERENCES donation_types(id),
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'TRY',
    frequency VARCHAR(20) NOT NULL,
    next_payment_date DATE NOT NULL,
    last_payment_date DATE,
    total_donated DECIMAL(12,2) DEFAULT 0,
    payment_count INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'active',
    card_token VARCHAR(255),
    failure_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bank_transfer_references (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_code VARCHAR(20) UNIQUE NOT NULL,
    campaign_id UUID REFERENCES campaigns(id),
    organization_id UUID REFERENCES organizations(id),
    donor_id UUID REFERENCES users(id),
    expected_amount DECIMAL(12,2),
    donation_type_id UUID REFERENCES donation_types(id),
    status VARCHAR(20) DEFAULT 'pending',
    matched_donation_id UUID REFERENCES donations(id),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
