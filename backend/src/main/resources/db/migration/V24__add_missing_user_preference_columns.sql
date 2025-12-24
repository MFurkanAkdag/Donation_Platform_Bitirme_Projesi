-- Add missing columns to user_preferences table to match UserPreference entity

-- Push notifications preference (mobile app)
ALTER TABLE user_preferences
    ADD COLUMN push_notifications BOOLEAN DEFAULT TRUE;

-- Weekly summary email preference
ALTER TABLE user_preferences
    ADD COLUMN weekly_summary_email BOOLEAN DEFAULT FALSE;

-- Campaign completion notification preference
ALTER TABLE user_preferences
    ADD COLUMN notify_on_campaign_complete BOOLEAN DEFAULT TRUE;

-- Campaign update notification preference
ALTER TABLE user_preferences
    ADD COLUMN notify_on_campaign_update BOOLEAN DEFAULT TRUE;
