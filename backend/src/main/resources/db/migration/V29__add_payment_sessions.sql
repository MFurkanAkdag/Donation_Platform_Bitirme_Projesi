-- Migration: Shopping Cart Support - Payment Sessions
-- Description: Allow multiple donations in a single payment transaction

-- =====================================================================
-- STEP 1: Create payment_sessions table
-- =====================================================================
CREATE TABLE IF NOT EXISTS payment_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    total_amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'TRY',
    status VARCHAR(50) NOT NULL DEFAULT 'pending', -- pending, processing, completed, failed
    payment_method VARCHAR(50),
    
    -- Session metadata
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_sessions_user ON payment_sessions(user_id);
CREATE INDEX idx_payment_sessions_status ON payment_sessions(status);

-- =====================================================================
-- STEP 2: Add payment_session_id to donations
-- =====================================================================
ALTER TABLE donations 
ADD COLUMN IF NOT EXISTS payment_session_id UUID REFERENCES payment_sessions(id);

CREATE INDEX idx_donations_payment_session ON donations(payment_session_id);

-- =====================================================================
-- STEP 3: Modify transactions table
-- =====================================================================
-- Add payment_session_id column
ALTER TABLE transactions 
ADD COLUMN IF NOT EXISTS payment_session_id UUID REFERENCES payment_sessions(id);

-- Note: We keep donation_id for backward compatibility
-- New flow: payment_session_id will be primary link
-- Old flow: donation_id still works for single donations

CREATE INDEX idx_transactions_payment_session ON transactions(payment_session_id);

-- =====================================================================
-- STEP 4: Data Migration (if needed)
-- =====================================================================
-- For existing single-donation transactions, create payment sessions
DO $$
DECLARE
    tx RECORD;
    new_session_id UUID;
BEGIN
    FOR tx IN 
        SELECT t.id, t.donation_id, d.donor_id, d.amount, d.currency, t.created_at
        FROM transactions t
        JOIN donations d ON t.donation_id = d.id
        WHERE t.payment_session_id IS NULL
    LOOP
        -- Create payment session for this transaction
        INSERT INTO payment_sessions (user_id, total_amount, currency, status, created_at, completed_at)
        VALUES (
            (SELECT donor_id FROM donations WHERE id = tx.donation_id),
            tx.amount,
            (SELECT currency FROM donations WHERE id = tx.donation_id),
            'completed',
            tx.created_at,
            tx.created_at
        )
        RETURNING id INTO new_session_id;
        
        -- Link transaction to session
        UPDATE transactions SET payment_session_id = new_session_id WHERE id = tx.id;
        
        -- Link donation to session
        UPDATE donations SET payment_session_id = new_session_id WHERE id = tx.donation_id;
    END LOOP;
END $$;

-- =====================================================================
-- COMMENTS
-- =====================================================================
COMMENT ON TABLE payment_sessions IS 'Holds payment session data for shopping cart functionality';
COMMENT ON COLUMN payment_sessions.total_amount IS 'Total amount of all donations in this payment session';
COMMENT ON COLUMN donations.payment_session_id IS 'Links donation to the payment session (shopping cart)';
COMMENT ON COLUMN transactions.payment_session_id IS 'Links transaction to the payment session';
