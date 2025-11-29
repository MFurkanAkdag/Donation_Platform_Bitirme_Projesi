-- Campaign tables
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
    evidence_deadline_days INTEGER DEFAULT 15,
    is_urgent BOOLEAN DEFAULT FALSE,
    is_featured BOOLEAN DEFAULT FALSE,
    location_city VARCHAR(100),
    location_district VARCHAR(100),
    beneficiary_count INTEGER,
    created_by UUID REFERENCES users(id),
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE campaign_categories (
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (campaign_id, category_id)
);

CREATE TABLE campaign_donation_types (
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    donation_type_id UUID NOT NULL REFERENCES donation_types(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (campaign_id, donation_type_id)
);

CREATE TABLE campaign_updates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(500),
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE campaign_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    caption VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE campaign_followers (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    notify_on_update BOOLEAN DEFAULT TRUE,
    notify_on_complete BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, campaign_id)
);
