// Mock data for organization dashboard (localStorage-backed)

import type {
  Organization,
  OrganizationContact,
  OrganizationBankAccount,
  OrganizationDocument,
  Campaign,
  TransparencyScore,
} from '@/types/organization';

const STORAGE_KEYS = {
  ORG: 'mock_org_data',
  CONTACTS: 'mock_org_contacts',
  BANK_ACCOUNTS: 'mock_org_bank_accounts',
  DOCUMENTS: 'mock_org_documents',
  CAMPAIGNS: 'mock_org_campaigns',
  TRANSPARENCY: 'mock_org_transparency',
};

// Initial mock organization (linked to foundation user)
const initialOrganization: Organization = {
  id: 'org_1',
  user_id: 'user_foundation_1',
  organization_type: 'foundation',
  legal_name: 'Hope Foundation Turkey',
  trade_name: 'Hope Foundation',
  description: 'Dedicated to improving lives through education and healthcare',
  mission_statement: 'We believe every child deserves access to quality education and healthcare',
  logo_url: 'https://via.placeholder.com/200',
  website_url: 'https://hopefoundation.org',
  verification_status: 'approved',
  is_featured: true,
  created_at: '2024-01-15T10:00:00Z',
  updated_at: '2024-12-20T14:30:00Z',
};

const initialContacts: OrganizationContact[] = [
  {
    id: 'contact_1',
    organization_id: 'org_1',
    contact_type: 'head_office',
    is_primary: true,
    email: 'info@hopefoundation.org',
    phone: '+90 555 123 4567',
    country: 'Turkey',
    city: 'Istanbul',
    district: 'Kadıköy',
    address_line1: 'Atatürk Street No:45',
    address_line2: 'Floor 3',
    created_at: '2024-01-15T10:00:00Z',
    updated_at: '2024-01-15T10:00:00Z',
  },
];

const initialBankAccounts: OrganizationBankAccount[] = [
  {
    id: 'bank_1',
    organization_id: 'org_1',
    bank_name: 'Ziraat Bankası',
    iban: 'TR330006100519786457841326',
    account_holder_name: 'Hope Foundation Turkey',
    currency: 'TRY',
    is_primary: true,
    is_verified: true,
    created_at: '2024-01-15T10:00:00Z',
    updated_at: '2024-01-15T10:00:00Z',
  },
];

const initialDocuments: OrganizationDocument[] = [
  {
    id: 'doc_1',
    organization_id: 'org_1',
    document_type: 'registration_certificate',
    document_name: '2024 Registration Certificate',
    file_url: 'https://example.com/docs/registration.pdf',
    is_verified: true,
    uploaded_at: '2024-01-15T10:00:00Z',
    expires_at: '2029-01-15',
    created_at: '2024-01-15T10:00:00Z',
    updated_at: '2024-01-15T10:00:00Z',
  },
  {
    id: 'doc_2',
    organization_id: 'org_1',
    document_type: 'tax_certificate',
    document_name: 'Tax Exemption Certificate 2024',
    file_url: 'https://example.com/docs/tax.pdf',
    is_verified: true,
    uploaded_at: '2024-01-20T10:00:00Z',
    expires_at: '2025-01-20',
    created_at: '2024-01-20T10:00:00Z',
    updated_at: '2024-01-20T10:00:00Z',
  },
];

const initialCampaigns: Campaign[] = [
  {
    id: 'camp_1',
    organization_id: 'org_1',
    category_id: 'cat_education',
    title: 'Books for Rural Schools',
    description: 'Providing essential textbooks and reading materials to rural schools',
    story: 'Many schools in rural areas lack basic educational materials...',
    goal_amount: 50000,
    collected_amount: 35000,
    currency: 'TRY',
    status: 'active',
    start_date: '2024-11-01',
    end_date: '2025-02-28',
    image_url: 'https://via.placeholder.com/400x300',
    is_featured: true,
    is_urgent: false,
    created_at: '2024-10-25T10:00:00Z',
    updated_at: '2024-12-20T10:00:00Z',
  },
  {
    id: 'camp_2',
    organization_id: 'org_1',
    category_id: 'cat_health',
    title: 'Mobile Health Clinic',
    description: 'Bringing healthcare to remote villages',
    story: 'Our mobile clinic visits 15 villages monthly...',
    goal_amount: 100000,
    collected_amount: 100000,
    currency: 'TRY',
    status: 'completed',
    start_date: '2024-06-01',
    end_date: '2024-11-30',
    image_url: 'https://via.placeholder.com/400x300',
    is_featured: false,
    is_urgent: false,
    created_at: '2024-05-20T10:00:00Z',
    updated_at: '2024-12-01T10:00:00Z',
  },
];

const initialTransparency: TransparencyScore = {
  id: 'trans_1',
  organization_id: 'org_1',
  current_score: 92,
  total_campaigns: 5,
  active_campaigns: 1,
  completed_campaigns: 4,
  total_reports: 12,
  verified_reports: 11,
  total_evidence: 45,
  verified_evidence: 43,
  avg_report_time_days: 3,
  calculated_at: '2024-12-20T00:00:00Z',
  created_at: '2024-12-20T00:00:00Z',
};

// Helper functions
function getFromStorage<T>(key: string, initial: T): T {
  if (typeof window === 'undefined') return initial;
  const stored = localStorage.getItem(key);
  return stored ? JSON.parse(stored) : initial;
}

function saveToStorage<T>(key: string, data: T): void {
  if (typeof window === 'undefined') return;
  localStorage.setItem(key, JSON.stringify(data));
}

// Public API
export const getOrganization = (): Organization => {
  return getFromStorage(STORAGE_KEYS.ORG, initialOrganization);
};

export const updateOrganization = (data: Partial<Organization>): Organization => {
  const current = getOrganization();
  const updated = { ...current, ...data, updated_at: new Date().toISOString() };
  saveToStorage(STORAGE_KEYS.ORG, updated);
  return updated;
};

export const getContacts = (): OrganizationContact[] => {
  return getFromStorage(STORAGE_KEYS.CONTACTS, initialContacts);
};

export const addContact = (contact: Omit<OrganizationContact, 'id' | 'created_at' | 'updated_at'>): OrganizationContact => {
  const contacts = getContacts();
  const newContact: OrganizationContact = {
    ...contact,
    id: `contact_${Date.now()}`,
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString(),
  };
  saveToStorage(STORAGE_KEYS.CONTACTS, [...contacts, newContact]);
  return newContact;
};

export const updateContact = (id: string, data: Partial<OrganizationContact>): OrganizationContact | null => {
  const contacts = getContacts();
  const index = contacts.findIndex(c => c.id === id);
  if (index === -1) return null;
  contacts[index] = { ...contacts[index], ...data, updated_at: new Date().toISOString() };
  saveToStorage(STORAGE_KEYS.CONTACTS, contacts);
  return contacts[index];
};

export const deleteContact = (id: string): boolean => {
  const contacts = getContacts();
  const filtered = contacts.filter(c => c.id !== id);
  if (filtered.length === contacts.length) return false;
  saveToStorage(STORAGE_KEYS.CONTACTS, filtered);
  return true;
};

export const getBankAccounts = (): OrganizationBankAccount[] => {
  return getFromStorage(STORAGE_KEYS.BANK_ACCOUNTS, initialBankAccounts);
};

export const addBankAccount = (account: Omit<OrganizationBankAccount, 'id' | 'created_at' | 'updated_at'>): OrganizationBankAccount => {
  const accounts = getBankAccounts();
  const newAccount: OrganizationBankAccount = {
    ...account,
    id: `bank_${Date.now()}`,
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString(),
  };
  saveToStorage(STORAGE_KEYS.BANK_ACCOUNTS, [...accounts, newAccount]);
  return newAccount;
};

export const updateBankAccount = (id: string, data: Partial<OrganizationBankAccount>): OrganizationBankAccount | null => {
  const accounts = getBankAccounts();
  const index = accounts.findIndex(a => a.id === id);
  if (index === -1) return null;
  accounts[index] = { ...accounts[index], ...data, updated_at: new Date().toISOString() };
  saveToStorage(STORAGE_KEYS.BANK_ACCOUNTS, accounts);
  return accounts[index];
};

export const deleteBankAccount = (id: string): boolean => {
  const accounts = getBankAccounts();
  const filtered = accounts.filter(a => a.id !== id);
  if (filtered.length === accounts.length) return false;
  saveToStorage(STORAGE_KEYS.BANK_ACCOUNTS, filtered);
  return true;
};

export const getDocuments = (): OrganizationDocument[] => {
  return getFromStorage(STORAGE_KEYS.DOCUMENTS, initialDocuments);
};

export const addDocument = (doc: Omit<OrganizationDocument, 'id' | 'created_at' | 'updated_at'>): OrganizationDocument => {
  const documents = getDocuments();
  const newDoc: OrganizationDocument = {
    ...doc,
    id: `doc_${Date.now()}`,
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString(),
  };
  saveToStorage(STORAGE_KEYS.DOCUMENTS, [...documents, newDoc]);
  return newDoc;
};

export const deleteDocument = (id: string): boolean => {
  const documents = getDocuments();
  const filtered = documents.filter(d => d.id !== id);
  if (filtered.length === documents.length) return false;
  saveToStorage(STORAGE_KEYS.DOCUMENTS, filtered);
  return true;
};

export const getCampaigns = (): Campaign[] => {
  return getFromStorage(STORAGE_KEYS.CAMPAIGNS, initialCampaigns);
};

export const getCampaignById = (id: string): Campaign | null => {
  const campaigns = getCampaigns();
  return campaigns.find(c => c.id === id) || null;
};

export const addCampaign = (campaign: Omit<Campaign, 'id' | 'created_at' | 'updated_at'>): Campaign => {
  const campaigns = getCampaigns();
  const newCampaign: Campaign = {
    ...campaign,
    id: `camp_${Date.now()}`,
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString(),
  };
  saveToStorage(STORAGE_KEYS.CAMPAIGNS, [...campaigns, newCampaign]);
  return newCampaign;
};

export const updateCampaign = (id: string, data: Partial<Campaign>): Campaign | null => {
  const campaigns = getCampaigns();
  const index = campaigns.findIndex(c => c.id === id);
  if (index === -1) return null;
  campaigns[index] = { ...campaigns[index], ...data, updated_at: new Date().toISOString() };
  saveToStorage(STORAGE_KEYS.CAMPAIGNS, campaigns);
  return campaigns[index];
};

export const deleteCampaign = (id: string): boolean => {
  const campaigns = getCampaigns();
  const filtered = campaigns.filter(c => c.id !== id);
  if (filtered.length === campaigns.length) return false;
  saveToStorage(STORAGE_KEYS.CAMPAIGNS, filtered);
  return true;
};

export const getTransparencyScore = (): TransparencyScore => {
  return getFromStorage(STORAGE_KEYS.TRANSPARENCY, initialTransparency);
};
