-- Reporting views
CREATE VIEW v_active_campaigns AS
SELECT
    c.id,
    c.title,
    c.slug,
    c.target_amount,
    c.collected_amount,
    c.donor_count,
    c.status,
    c.end_date,
    o.legal_name AS organization_name,
    o.logo_url AS organization_logo,
    ts.current_score AS transparency_score,
    ROUND((c.collected_amount / c.target_amount * 100), 2) AS progress_percentage
FROM campaigns c
JOIN organizations o ON c.organization_id = o.id
LEFT JOIN transparency_scores ts ON o.id = ts.organization_id
WHERE c.status = 'active'
ORDER BY c.is_featured DESC, c.created_at DESC;

CREATE VIEW v_organization_summary AS
SELECT
    o.id,
    o.legal_name,
    o.organization_type,
    o.verification_status,
    o.logo_url,
    ts.current_score AS transparency_score,
    COUNT(DISTINCT c.id) AS total_campaigns,
    COUNT(DISTINCT CASE WHEN c.status = 'active' THEN c.id END) AS active_campaigns,
    COALESCE(SUM(c.collected_amount), 0) AS total_collected
FROM organizations o
LEFT JOIN transparency_scores ts ON o.id = ts.organization_id
LEFT JOIN campaigns c ON o.id = c.organization_id
WHERE o.verification_status = 'approved'
GROUP BY o.id, ts.current_score;
