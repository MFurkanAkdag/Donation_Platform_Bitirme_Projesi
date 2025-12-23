// Database-aligned types for admin panel

export type UserRole = 'admin' | 'donor' | 'foundation' | 'beneficiary';
export type UserStatus = 'active' | 'inactive' | 'suspended' | 'banned';
export type VerificationStatus = 'pending' | 'in_review' | 'approved' | 'rejected';
export type CampaignStatus = 'draft' | 'pending_approval' | 'active' | 'paused' | 'completed' | 'cancelled';
export type DonationStatus = 'pending' | 'completed' | 'failed' | 'refunded';
export type EvidenceStatus = 'pending' | 'approved' | 'rejected';
export type ReportStatus = 'pending' | 'in_review' | 'resolved' | 'dismissed';
export type ReportPriority = 'low' | 'medium' | 'high' | 'critical';
export type ReportType = 'fraud' | 'inappropriate_content' | 'financial_issue' | 'technical_issue' | 'other';
export type EntityType = 'user' | 'organization' | 'campaign' | 'donation' | 'evidence' | 'report';

export interface AdminUser {
  id: string;
  email: string;
  role: UserRole;
  status: UserStatus;
  email_verified: boolean;
  failed_login_attempts: number;
  locked_until?: string;
  created_at: string;
  updated_at: string;
}

export interface AdminOrganization {
  id: string;
  user_id: string;
  legal_name: string;
  trade_name?: string;
  organization_type: 'foundation' | 'association' | 'ngo';
  verification_status: VerificationStatus;
  is_featured: boolean;
  verified_at?: string;
  rejection_reason?: string;
  created_at: string;
  updated_at: string;
}

export interface AdminCampaign {
  id: string;
  organization_id: string;
  title: string;
  status: CampaignStatus;
  goal_amount: number;
  collected_amount: number;
  donor_count: number;
  currency: string;
  is_featured: boolean;
  is_urgent: boolean;
  created_at: string;
  updated_at: string;
}

export interface AdminDonation {
  id: string;
  user_id: string;
  campaign_id: string;
  amount: number;
  currency: string;
  status: DonationStatus;
  is_anonymous: boolean;
  payment_method?: string;
  created_at: string;
}

export interface AdminEvidence {
  id: string;
  campaign_id: string;
  title: string;
  description?: string;
  evidence_type: 'receipt' | 'invoice' | 'photo' | 'video' | 'report' | 'other';
  status: EvidenceStatus;
  amount_spent?: number;
  vendor_name?: string;
  uploaded_at: string;
  reviewed_at?: string;
  reviewer_id?: string;
}

export interface AdminReport {
  id: string;
  reporter_id: string;
  report_type: ReportType;
  entity_type: EntityType;
  entity_id: string;
  status: ReportStatus;
  priority: ReportPriority;
  description: string;
  assigned_to?: string;
  resolved_at?: string;
  created_at: string;
  updated_at: string;
}

export interface SystemSetting {
  id: string;
  setting_key: string;
  setting_value: string;
  value_type: 'string' | 'number' | 'boolean' | 'json';
  is_public: boolean;
  description?: string;
  updated_at: string;
}

export interface AuditLog {
  id: string;
  user_id: string;
  action: string;
  entity_type: EntityType;
  entity_id: string;
  old_values?: Record<string, any>;
  new_values?: Record<string, any>;
  ip_address?: string;
  user_agent?: string;
  created_at: string;
}

export interface DashboardStats {
  total_users: number;
  pending_org_verifications: number;
  pending_campaign_approvals: number;
  pending_evidence_reviews: number;
  pending_reports: number;
  total_donations_today: number;
  total_amount_today: number;
}
