-- Evidence and transparency tables
CREATE TABLE evidences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaigns(id),
    evidence_type evidence_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    amount_spent DECIMAL(12,2),
    vendor_name VARCHAR(255),
    vendor_tax_number VARCHAR(20),
    status evidence_status DEFAULT 'pending',
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    rejection_reason TEXT,
    uploaded_by UUID REFERENCES users(id),
    uploaded_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

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

CREATE TABLE transparency_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID UNIQUE NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    current_score DECIMAL(5,2) DEFAULT 50.00,
    total_campaigns INTEGER DEFAULT 0,
    completed_campaigns INTEGER DEFAULT 0,
    on_time_reports INTEGER DEFAULT 0,
    late_reports INTEGER DEFAULT 0,
    approved_evidences INTEGER DEFAULT 0,
    rejected_evidences INTEGER DEFAULT 0,
    last_calculated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transparency_score_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    previous_score DECIMAL(5,2),
    new_score DECIMAL(5,2) NOT NULL,
    change_reason VARCHAR(255) NOT NULL,
    campaign_id UUID REFERENCES campaigns(id),
    evidence_id UUID REFERENCES evidences(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
