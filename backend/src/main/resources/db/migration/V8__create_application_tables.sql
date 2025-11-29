-- Application tables
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
    household_size INTEGER,
    urgency_level INTEGER DEFAULT 1,
    assigned_organization_id UUID REFERENCES organizations(id),
    assigned_campaign_id UUID REFERENCES campaigns(id),
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE application_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    document_type VARCHAR(100) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    uploaded_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
