-- Create Receipts table (Normalized - Linking Donation to Unique Barcode)
CREATE TABLE IF NOT EXISTS receipts (
    id BIGSERIAL PRIMARY KEY,
    donation_id UUID NOT NULL UNIQUE,
    barcode_data VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_receipt_donation FOREIGN KEY (donation_id) REFERENCES donations(id) ON DELETE CASCADE
);

-- Add Realization columns to Campaigns table
ALTER TABLE campaigns 
ADD COLUMN IF NOT EXISTS realization_deadline TIMESTAMP WITH TIME ZONE,
ADD COLUMN IF NOT EXISTS realization_status VARCHAR(255) DEFAULT 'NOT_STARTED';

-- Comment: ensure realization_status is handled as STRING by backend logic to avoid Enum Type issues
