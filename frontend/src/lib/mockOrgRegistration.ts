// Mock organization registration types and logic

export type OrganizationRegistrationPayload = {
  user: {
    email: string;
    password: string;
    role: string;
  };
  organization: {
    organization_type: 'foundation' | 'association' | 'ngo';
    legal_name: string;
    trade_name?: string;
    website_url?: string;
    logo_url?: string;
    description: string;
    mission_statement?: string;
    verification_status: 'pending';
  };
  primaryContact: {
    contact_type: string;
    is_primary: true;
    email: string;
    phone: string;
    country: string;
    city: string;
    district?: string;
    address_line1: string;
    address_line2?: string;
  };
  bankAccount: {
    bank_name: string;
    iban: string;
    currency: string;
    is_primary: true;
    is_verified: false;
  };
  documents: Array<{
    document_type: string;
    document_name: string;
    file_url: string;
    is_verified: false;
    expires_at?: string;
  }>;
};

export const submitOrganizationRegistration = async (
  payload: OrganizationRegistrationPayload
): Promise<{ success: boolean; error?: string }> => {
  // Simulate API delay
  await new Promise(resolve => setTimeout(resolve, 1500));

  try {
    // Store in localStorage
    localStorage.setItem('mock_org_registration', JSON.stringify(payload));
    
    // Create mock auth user for organization
    const mockOrgUser = {
      id: `org_${Date.now()}`,
      email: payload.user.email,
      role: payload.organization.organization_type,
      firstName: payload.organization.legal_name.split(' ')[0],
      lastName: '',
      displayName: payload.organization.legal_name,
    };
    
    localStorage.setItem('mockAuthUser', JSON.stringify(mockOrgUser));
    
    return { success: true };
  } catch (error) {
    return { success: false, error: 'Failed to submit registration' };
  }
};

export const getOrganizationRegistration = (): OrganizationRegistrationPayload | null => {
  const stored = localStorage.getItem('mock_org_registration');
  return stored ? JSON.parse(stored) : null;
};
