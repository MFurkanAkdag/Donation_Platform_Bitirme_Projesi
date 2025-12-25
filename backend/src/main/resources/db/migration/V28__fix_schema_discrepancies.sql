-- Fix Organization Schema
-- Dropping view because it depends on organization_type enum type
DROP VIEW IF EXISTS v_organization_summary CASCADE;
ALTER TABLE organizations ALTER COLUMN organization_type TYPE VARCHAR(50);
ALTER TABLE organizations ALTER COLUMN verification_status TYPE VARCHAR(50);

-- Fix Transparency Score Schema
ALTER TABLE transparency_scores ADD COLUMN IF NOT EXISTS evidence_score DECIMAL(5,2) DEFAULT 0.0;
ALTER TABLE transparency_scores ADD COLUMN IF NOT EXISTS report_score DECIMAL(5,2) DEFAULT 0.0;
ALTER TABLE transparency_scores ADD COLUMN IF NOT EXISTS timeliness_score DECIMAL(5,2) DEFAULT 0.0;
ALTER TABLE transparency_scores ADD COLUMN IF NOT EXISTS total_evidences INTEGER DEFAULT 0;

ALTER TABLE transparency_score_history ADD COLUMN IF NOT EXISTS change_amount DECIMAL(5,2);
ALTER TABLE transparency_score_history ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE transparency_score_history ADD COLUMN IF NOT EXISTS related_entity_id UUID;
ALTER TABLE transparency_score_history ADD COLUMN IF NOT EXISTS related_entity_type VARCHAR(50);

-- Fix Campaign Schema
-- Dropping views because they depend on status enum type
DROP VIEW IF EXISTS v_active_campaigns CASCADE;
DROP VIEW IF EXISTS v_campaign_summary CASCADE;
ALTER TABLE campaigns ALTER COLUMN status TYPE VARCHAR(50);
ALTER TABLE campaigns ADD COLUMN IF NOT EXISTS extension_count INTEGER DEFAULT 0;
ALTER TABLE campaigns ADD COLUMN IF NOT EXISTS slug VARCHAR(255);
ALTER TABLE campaigns ADD COLUMN IF NOT EXISTS is_featured BOOLEAN DEFAULT false;

-- Fix Donation Schema
ALTER TABLE donations ALTER COLUMN ip_address TYPE VARCHAR(45) USING ip_address::VARCHAR;
ALTER TABLE donations ALTER COLUMN status TYPE VARCHAR(50);
ALTER TABLE donations ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50);
ALTER TABLE donations ADD COLUMN IF NOT EXISTS transaction_id VARCHAR(255);
ALTER TABLE donations ADD COLUMN IF NOT EXISTS source VARCHAR(50);
ALTER TABLE donations ADD COLUMN IF NOT EXISTS user_agent TEXT;
ALTER TABLE donations ADD COLUMN IF NOT EXISTS refund_status VARCHAR(50);
ALTER TABLE donations ADD COLUMN IF NOT EXISTS refund_reason TEXT;
ALTER TABLE donations ADD COLUMN IF NOT EXISTS refund_requested_at TIMESTAMP WITH TIME ZONE;
