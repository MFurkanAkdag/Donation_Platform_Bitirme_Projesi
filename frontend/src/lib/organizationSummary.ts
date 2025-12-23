// Type matching v_organization_summary view

export interface OrganizationSummary {
  organization_id: string;
  legal_name: string;
  trade_name?: string;
  organization_type: 'foundation' | 'association' | 'ngo';
  verification_status: 'pending' | 'in_review' | 'approved' | 'rejected';
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

// Mock data matching v_organization_summary view
export const mockOrganizationSummary: OrganizationSummary[] = [
  {
    organization_id: '1',
    legal_name: 'International Relief Fund',
    trade_name: 'IRF Global',
    organization_type: 'foundation',
    verification_status: 'approved',
    logo_url: 'https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?w=200&h=200&fit=crop',
    city: 'New York',
    country: 'United States',
    current_score: 94,
    total_campaigns: 12,
    active_campaigns: 4,
    completed_campaigns: 8,
    total_collected: 567500,
    is_featured: true,
  },
  {
    organization_id: '2',
    legal_name: 'Hope for Children Foundation',
    organization_type: 'foundation',
    verification_status: 'approved',
    logo_url: 'https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=200&h=200&fit=crop',
    city: 'London',
    country: 'United Kingdom',
    current_score: 87,
    total_campaigns: 8,
    active_campaigns: 2,
    completed_campaigns: 6,
    total_collected: 413500,
    is_featured: true,
  },
  {
    organization_id: '3',
    legal_name: 'Clean Water Initiative',
    trade_name: 'CWI',
    organization_type: 'ngo',
    verification_status: 'approved',
    logo_url: 'https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=200&h=200&fit=crop',
    city: 'Geneva',
    country: 'Switzerland',
    current_score: 91,
    total_campaigns: 15,
    active_campaigns: 3,
    completed_campaigns: 12,
    total_collected: 892000,
    is_featured: true,
  },
  {
    organization_id: '4',
    legal_name: 'Community Healthcare Association',
    organization_type: 'association',
    verification_status: 'approved',
    city: 'Mumbai',
    country: 'India',
    current_score: 82,
    total_campaigns: 5,
    active_campaigns: 2,
    completed_campaigns: 3,
    total_collected: 185000,
    is_featured: false,
  },
  {
    organization_id: '5',
    legal_name: 'Green Earth Alliance',
    organization_type: 'ngo',
    verification_status: 'in_review',
    city: 'Sydney',
    country: 'Australia',
    total_campaigns: 2,
    active_campaigns: 2,
    completed_campaigns: 0,
    total_collected: 45000,
    is_featured: false,
  },
];
