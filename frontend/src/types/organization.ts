// Database-aligned types for organizations

export type OrganizationType = 'foundation' | 'association' | 'ngo';
export type VerificationStatus = 'pending' | 'in_review' | 'approved' | 'rejected';
export type ContactType = 'head_office' | 'branch' | 'billing' | 'technical' | 'other';
export type DocumentType = 'registration_certificate' | 'tax_certificate' | 'bank_letter' | 'activity_report' | 'financial_statement' | 'other';
export type CampaignStatus = 'draft' | 'pending_approval' | 'active' | 'paused' | 'completed' | 'cancelled';

export interface Organization {
  id: string;
  user_id: string;
  organization_type: OrganizationType;
  legal_name: string;
  trade_name?: string;
  description: string;
  mission_statement?: string;
  logo_url?: string;
  website_url?: string;
  verification_status: VerificationStatus;
  is_featured: boolean;
  created_at: string;
  updated_at: string;
}

export interface OrganizationContact {
  id: string;
  organization_id: string;
  contact_type: ContactType;
  is_primary: boolean;
  email: string;
  phone: string;
  country?: string;
  city?: string;
  district?: string;
  address_line1?: string;
  address_line2?: string;
  created_at: string;
  updated_at: string;
}

export interface OrganizationBankAccount {
  id: string;
  organization_id: string;
  bank_name: string;
  iban: string;
  account_holder_name?: string;
  currency: string;
  is_primary: boolean;
  is_verified: boolean;
  created_at: string;
  updated_at: string;
}

export interface OrganizationDocument {
  id: string;
  organization_id: string;
  document_type: DocumentType;
  document_name: string;
  file_url: string;
  is_verified: boolean;
  uploaded_at: string;
  expires_at?: string;
  created_at: string;
  updated_at: string;
}

export interface Campaign {
  id: string;
  organization_id: string;
  category_id: string;
  title: string;
  description: string;
  story?: string;
  goal_amount: number;
  collected_amount: number;
  currency: string;
  status: CampaignStatus;
  start_date: string;
  end_date: string;
  image_url?: string;
  video_url?: string;
  is_featured: boolean;
  is_urgent: boolean;
  created_at: string;
  updated_at: string;
}

export interface TransparencyScore {
  id: string;
  organization_id: string;
  current_score: number;
  total_campaigns: number;
  active_campaigns: number;
  completed_campaigns: number;
  total_reports: number;
  verified_reports: number;
  total_evidence: number;
  verified_evidence: number;
  avg_report_time_days?: number;
  calculated_at: string;
  created_at: string;
}

export interface OrganizationSummary {
  organization_id: string;
  legal_name: string;
  trade_name?: string;
  organization_type: OrganizationType;
  verification_status: VerificationStatus;
  logo_url?: string;
  city?: string;
  country?: string;
  current_score?: number;
  total_campaigns: number;
  active_campaigns: number;
  completed_campaigns: number;
  total_collected: number;
  is_featured: boolean;
}
