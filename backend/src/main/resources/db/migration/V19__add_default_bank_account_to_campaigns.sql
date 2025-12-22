-- Add default_bank_account_id column to campaigns table
ALTER TABLE campaigns 
ADD COLUMN default_bank_account_id UUID REFERENCES organization_bank_accounts(id);
