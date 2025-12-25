-- Add missing short_description column to campaigns table
-- This column was defined in the Campaign entity but missing from the schema

ALTER TABLE campaigns 
ADD COLUMN IF NOT EXISTS short_description VARCHAR(500);

-- Comment: This ensures the backend can query campaigns without the "column does not exist" error
