-- Add cart_items JSON column to payment_sessions table
-- This allows storing cart items (campaignId + amount) before checkout
-- Donations are created only at checkout time

ALTER TABLE payment_sessions
ADD COLUMN cart_items JSONB DEFAULT '[]'::jsonb;

COMMENT ON COLUMN payment_sessions.cart_items IS 'Shopping cart items before checkout (array of {campaignId, amount, currency})';
