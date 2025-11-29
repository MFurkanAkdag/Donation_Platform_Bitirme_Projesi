-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_sensitive_user_id ON user_sensitive_data(user_id);

CREATE INDEX idx_organizations_user_id ON organizations(user_id);
CREATE INDEX idx_organizations_verification ON organizations(verification_status);
CREATE INDEX idx_organizations_type ON organizations(organization_type);
CREATE INDEX idx_org_contacts_org_id ON organization_contacts(organization_id);
CREATE INDEX idx_org_docs_org_id ON organization_documents(organization_id);
CREATE INDEX idx_org_docs_type ON organization_documents(document_type);
CREATE INDEX idx_org_bank_org_id ON organization_bank_accounts(organization_id);

CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_categories_parent ON categories(parent_id);

CREATE INDEX idx_campaigns_org_id ON campaigns(organization_id);
CREATE INDEX idx_campaigns_status ON campaigns(status);
CREATE INDEX idx_campaigns_slug ON campaigns(slug);
CREATE INDEX idx_campaigns_dates ON campaigns(start_date, end_date);
CREATE INDEX idx_campaigns_featured ON campaigns(is_featured) WHERE is_featured = TRUE;
CREATE INDEX idx_campaign_categories_campaign ON campaign_categories(campaign_id);
CREATE INDEX idx_campaign_categories_category ON campaign_categories(category_id);
CREATE INDEX idx_campaign_updates_campaign ON campaign_updates(campaign_id);
CREATE INDEX idx_campaign_images_campaign ON campaign_images(campaign_id);
CREATE INDEX idx_campaign_followers_campaign ON campaign_followers(campaign_id);

CREATE INDEX idx_donations_campaign ON donations(campaign_id);
CREATE INDEX idx_donations_donor ON donations(donor_id);
CREATE INDEX idx_donations_status ON donations(status);
CREATE INDEX idx_donations_created ON donations(created_at);
CREATE INDEX idx_transactions_donation ON transactions(donation_id);
CREATE INDEX idx_transactions_provider_id ON transactions(provider_transaction_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_recurring_donor ON recurring_donations(donor_id);
CREATE INDEX idx_recurring_next_payment ON recurring_donations(next_payment_date) WHERE status = 'active';
CREATE INDEX idx_bank_ref_code ON bank_transfer_references(reference_code);
CREATE INDEX idx_bank_ref_status ON bank_transfer_references(status) WHERE status = 'pending';

CREATE INDEX idx_evidences_campaign ON evidences(campaign_id);
CREATE INDEX idx_evidences_status ON evidences(status);
CREATE INDEX idx_evidence_docs_evidence ON evidence_documents(evidence_id);
CREATE INDEX idx_transparency_org ON transparency_scores(organization_id);
CREATE INDEX idx_transparency_score ON transparency_scores(current_score);
CREATE INDEX idx_score_history_org ON transparency_score_history(organization_id);
CREATE INDEX idx_score_history_created ON transparency_score_history(created_at);

CREATE INDEX idx_applications_applicant ON applications(applicant_id);
CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_applications_category ON applications(category_id);
CREATE INDEX idx_app_docs_application ON application_documents(application_id);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);
CREATE INDEX idx_email_logs_user ON email_logs(user_id);
CREATE INDEX idx_email_logs_type ON email_logs(email_type);
CREATE INDEX idx_reports_entity ON reports(entity_type, entity_id);
CREATE INDEX idx_reports_status ON reports(status);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_password_reset_user ON password_reset_tokens(user_id);
