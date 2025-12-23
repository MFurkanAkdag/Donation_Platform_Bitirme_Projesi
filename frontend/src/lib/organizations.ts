// Types matching database schema

export type OrganizationType = 'foundation' | 'association' | 'ngo';
export type VerificationStatus = 'pending' | 'in_review' | 'approved' | 'rejected';
export type ContactType = 'primary' | 'billing' | 'support' | 'legal';
export type DocumentType = 'registration' | 'tax_exemption' | 'financial_report' | 'license' | 'other';

export interface Organization {
  id: string;
  organization_type: OrganizationType;
  legal_name: string;
  trade_name?: string;
  description: string;
  mission_statement: string;
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
  email?: string;
  phone?: string;
  address_line1?: string;
  address_line2?: string;
  city?: string;
  district?: string;
  postal_code?: string;
  country: string;
  is_primary: boolean;
  created_at: string;
}

export interface OrganizationDocument {
  id: string;
  organization_id: string;
  document_type: DocumentType;
  document_name: string;
  file_url: string;
  is_verified: boolean;
  expires_at?: string;
  uploaded_at: string;
  verified_at?: string;
}

export interface OrganizationBankAccount {
  id: string;
  organization_id: string;
  bank_name: string;
  iban: string;
  currency: string;
  is_verified: boolean;
  is_primary: boolean;
  created_at: string;
}

export interface TransparencyScore {
  id: string;
  organization_id: string;
  current_score: number;
  total_campaigns: number;
  completed_campaigns: number;
  on_time_reports: number;
  late_reports: number;
  approved_evidences: number;
  rejected_evidences: number;
  last_calculated_at: string;
}

// Mock data
export const mockOrganizations: Organization[] = [
  {
    id: '1',
    organization_type: 'foundation',
    legal_name: 'International Relief Fund',
    trade_name: 'IRF Global',
    description: 'Providing emergency aid and long-term development programs worldwide',
    mission_statement: 'To alleviate suffering and support sustainable development in communities affected by poverty, conflict, and natural disasters.',
    logo_url: 'https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?w=200&h=200&fit=crop',
    website_url: 'https://example.org/irf',
    verification_status: 'approved',
    is_featured: true,
    created_at: '2020-01-15T00:00:00Z',
    updated_at: '2024-12-01T00:00:00Z',
  },
  {
    id: '2',
    organization_type: 'foundation',
    legal_name: 'Hope for Children Foundation',
    description: 'Supporting education and healthcare for children in need',
    mission_statement: 'To ensure every child has access to quality education and healthcare, regardless of their circumstances.',
    logo_url: 'https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=200&h=200&fit=crop',
    website_url: 'https://example.org/hope',
    verification_status: 'approved',
    is_featured: true,
    created_at: '2019-06-10T00:00:00Z',
    updated_at: '2024-11-20T00:00:00Z',
  },
  {
    id: '3',
    organization_type: 'ngo',
    legal_name: 'Clean Water Initiative',
    trade_name: 'CWI',
    description: 'Building sustainable water infrastructure in developing regions',
    mission_statement: 'To provide clean, safe drinking water to underserved communities through sustainable infrastructure projects.',
    logo_url: 'https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=200&h=200&fit=crop',
    website_url: 'https://example.org/cwi',
    verification_status: 'approved',
    is_featured: true,
    created_at: '2021-03-20T00:00:00Z',
    updated_at: '2024-12-05T00:00:00Z',
  },
  {
    id: '4',
    organization_type: 'association',
    legal_name: 'Community Healthcare Association',
    description: 'Providing medical services to rural communities',
    mission_statement: 'To improve healthcare access and outcomes in underserved rural areas.',
    verification_status: 'approved',
    is_featured: false,
    created_at: '2022-08-12T00:00:00Z',
    updated_at: '2024-10-15T00:00:00Z',
  },
  {
    id: '5',
    organization_type: 'ngo',
    legal_name: 'Green Earth Alliance',
    description: 'Environmental conservation and climate action',
    mission_statement: 'To protect our planet through conservation, education, and sustainable practices.',
    verification_status: 'in_review',
    is_featured: false,
    created_at: '2023-02-28T00:00:00Z',
    updated_at: '2024-11-30T00:00:00Z',
  },
];

export const mockOrganizationContacts: OrganizationContact[] = [
  {
    id: 'c1',
    organization_id: '1',
    contact_type: 'primary',
    email: 'contact@irf.org',
    phone: '+1-555-0100',
    address_line1: '123 Aid Street',
    city: 'New York',
    district: 'Manhattan',
    postal_code: '10001',
    country: 'United States',
    is_primary: true,
    created_at: '2020-01-15T00:00:00Z',
  },
  {
    id: 'c2',
    organization_id: '2',
    contact_type: 'primary',
    email: 'info@hopeforchildren.org',
    phone: '+44-20-7123-4567',
    address_line1: '456 Hope Avenue',
    city: 'London',
    postal_code: 'SW1A 1AA',
    country: 'United Kingdom',
    is_primary: true,
    created_at: '2019-06-10T00:00:00Z',
  },
  {
    id: 'c3',
    organization_id: '3',
    contact_type: 'primary',
    email: 'hello@cleanwater.org',
    phone: '+41-22-123-4567',
    address_line1: '789 Water Lane',
    city: 'Geneva',
    postal_code: '1200',
    country: 'Switzerland',
    is_primary: true,
    created_at: '2021-03-20T00:00:00Z',
  },
  {
    id: 'c4',
    organization_id: '4',
    contact_type: 'primary',
    email: 'contact@healthcare.org',
    phone: '+91-11-2345-6789',
    address_line1: '321 Medical Road',
    city: 'Mumbai',
    district: 'Maharashtra',
    postal_code: '400001',
    country: 'India',
    is_primary: true,
    created_at: '2022-08-12T00:00:00Z',
  },
];

export const mockOrganizationDocuments: OrganizationDocument[] = [
  {
    id: 'd1',
    organization_id: '1',
    document_type: 'registration',
    document_name: 'IRF Registration Certificate',
    file_url: '/documents/irf-registration.pdf',
    is_verified: true,
    uploaded_at: '2020-01-15T00:00:00Z',
    verified_at: '2020-01-20T00:00:00Z',
  },
  {
    id: 'd2',
    organization_id: '1',
    document_type: 'tax_exemption',
    document_name: 'Tax Exemption Certificate',
    file_url: '/documents/irf-tax-exemption.pdf',
    is_verified: true,
    expires_at: '2025-12-31T00:00:00Z',
    uploaded_at: '2020-02-01T00:00:00Z',
    verified_at: '2020-02-05T00:00:00Z',
  },
  {
    id: 'd3',
    organization_id: '2',
    document_type: 'registration',
    document_name: 'Hope Foundation Registration',
    file_url: '/documents/hope-registration.pdf',
    is_verified: true,
    uploaded_at: '2019-06-10T00:00:00Z',
    verified_at: '2019-06-15T00:00:00Z',
  },
];

export const mockOrganizationBankAccounts: OrganizationBankAccount[] = [
  {
    id: 'b1',
    organization_id: '1',
    bank_name: 'Chase Bank',
    iban: 'US12XXXXXXXXXXXX3456',
    currency: 'USD',
    is_verified: true,
    is_primary: true,
    created_at: '2020-01-15T00:00:00Z',
  },
  {
    id: 'b2',
    organization_id: '2',
    bank_name: 'HSBC UK',
    iban: 'GB29XXXXXXXXXXXX7890',
    currency: 'GBP',
    is_verified: true,
    is_primary: true,
    created_at: '2019-06-10T00:00:00Z',
  },
  {
    id: 'b3',
    organization_id: '3',
    bank_name: 'UBS Switzerland',
    iban: 'CH93XXXXXXXXXXXX1234',
    currency: 'CHF',
    is_verified: true,
    is_primary: true,
    created_at: '2021-03-20T00:00:00Z',
  },
];

export const mockTransparencyScores: TransparencyScore[] = [
  {
    id: 't1',
    organization_id: '1',
    current_score: 94,
    total_campaigns: 12,
    completed_campaigns: 8,
    on_time_reports: 7,
    late_reports: 1,
    approved_evidences: 45,
    rejected_evidences: 2,
    last_calculated_at: '2024-12-01T00:00:00Z',
  },
  {
    id: 't2',
    organization_id: '2',
    current_score: 87,
    total_campaigns: 8,
    completed_campaigns: 6,
    on_time_reports: 5,
    late_reports: 1,
    approved_evidences: 32,
    rejected_evidences: 4,
    last_calculated_at: '2024-11-28T00:00:00Z',
  },
  {
    id: 't3',
    organization_id: '3',
    current_score: 91,
    total_campaigns: 15,
    completed_campaigns: 12,
    on_time_reports: 11,
    late_reports: 1,
    approved_evidences: 58,
    rejected_evidences: 3,
    last_calculated_at: '2024-12-05T00:00:00Z',
  },
  {
    id: 't4',
    organization_id: '4',
    current_score: 82,
    total_campaigns: 5,
    completed_campaigns: 3,
    on_time_reports: 2,
    late_reports: 1,
    approved_evidences: 18,
    rejected_evidences: 2,
    last_calculated_at: '2024-11-15T00:00:00Z',
  },
];
