-- Add ID column to join tables that extend BaseEntity but use composite keys in DB
-- Tables: campaign_categories, campaign_donation_types, campaign_followers

-- campaign_categories
ALTER TABLE campaign_categories ADD COLUMN id UUID DEFAULT gen_random_uuid();
ALTER TABLE campaign_categories DROP CONSTRAINT campaign_categories_pkey;
ALTER TABLE campaign_categories ADD PRIMARY KEY (id);
ALTER TABLE campaign_categories ADD CONSTRAINT campaign_categories_unique_key UNIQUE (campaign_id, category_id);

-- campaign_donation_types
ALTER TABLE campaign_donation_types ADD COLUMN id UUID DEFAULT gen_random_uuid();
ALTER TABLE campaign_donation_types DROP CONSTRAINT campaign_donation_types_pkey;
ALTER TABLE campaign_donation_types ADD PRIMARY KEY (id);
ALTER TABLE campaign_donation_types ADD CONSTRAINT campaign_donation_types_unique_key UNIQUE (campaign_id, donation_type_id);

-- campaign_followers
ALTER TABLE campaign_followers ADD COLUMN id UUID DEFAULT gen_random_uuid();
ALTER TABLE campaign_followers DROP CONSTRAINT campaign_followers_pkey;
ALTER TABLE campaign_followers ADD PRIMARY KEY (id);
ALTER TABLE campaign_followers ADD CONSTRAINT campaign_followers_unique_key UNIQUE (user_id, campaign_id);
