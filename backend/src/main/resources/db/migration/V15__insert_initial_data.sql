-- Seed data for system settings
INSERT INTO system_settings (setting_key, setting_value, value_type, description, is_public) VALUES
('platform_name', 'Şeffaf Bağış Platformu', 'string', 'Platform adı', TRUE),
('min_donation_amount', '10', 'number', 'Minimum bağış tutarı (TRY)', TRUE),
('max_donation_amount', '1000000', 'number', 'Maximum bağış tutarı (TRY)', TRUE),
('evidence_deadline_days', '15', 'number', 'Varsayılan kanıt yükleme süresi (gün)', FALSE),
('transparency_score_threshold', '40', 'number', 'Kampanya açma için minimum skor', FALSE),
('commission_rate', '0', 'number', 'Platform komisyon oranı (%)', FALSE),
('maintenance_mode', 'false', 'boolean', 'Bakım modu aktif mi', TRUE)
ON CONFLICT (setting_key) DO NOTHING;
