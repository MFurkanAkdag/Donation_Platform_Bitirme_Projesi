// Admin data store with localStorage persistence

import type {
  AdminUser,
  AdminOrganization,
  AdminCampaign,
  AdminDonation,
  AdminEvidence,
  AdminReport,
  SystemSetting,
  AuditLog,
} from '@/types/admin';

const STORAGE_KEYS = {
  USERS: 'admin_users',
  ORGANIZATIONS: 'admin_organizations',
  CAMPAIGNS: 'admin_campaigns',
  DONATIONS: 'admin_donations',
  EVIDENCES: 'admin_evidences',
  REPORTS: 'admin_reports',
  SETTINGS: 'admin_settings',
  AUDIT_LOGS: 'admin_audit_logs',
};

function getFromStorage<T>(key: string, initial: T): T {
  if (typeof window === 'undefined') return initial;
  const stored = localStorage.getItem(key);
  return stored ? JSON.parse(stored) : initial;
}

function saveToStorage<T>(key: string, data: T): void {
  if (typeof window === 'undefined') return;
  localStorage.setItem(key, JSON.stringify(data));
}

// Generic CRUD operations
export const adminDataStore = {
  // Organizations
  organizations: {
    getAll: (): AdminOrganization[] => {
      const { mockOrganizations } = require('./mock-data/admin');
      return getFromStorage(STORAGE_KEYS.ORGANIZATIONS, mockOrganizations);
    },
    getById: (id: string): AdminOrganization | null => {
      const orgs = adminDataStore.organizations.getAll();
      return orgs.find(o => o.id === id) || null;
    },
    update: (id: string, data: Partial<AdminOrganization>): AdminOrganization | null => {
      const orgs = adminDataStore.organizations.getAll();
      const index = orgs.findIndex(o => o.id === id);
      if (index === -1) return null;
      orgs[index] = { ...orgs[index], ...data, updated_at: new Date().toISOString() };
      saveToStorage(STORAGE_KEYS.ORGANIZATIONS, orgs);
      return orgs[index];
    },
  },

  // Campaigns
  campaigns: {
    getAll: (): AdminCampaign[] => {
      const { mockCampaigns } = require('./mock-data/admin');
      return getFromStorage(STORAGE_KEYS.CAMPAIGNS, mockCampaigns);
    },
    getById: (id: string): AdminCampaign | null => {
      const campaigns = adminDataStore.campaigns.getAll();
      return campaigns.find(c => c.id === id) || null;
    },
    update: (id: string, data: Partial<AdminCampaign>): AdminCampaign | null => {
      const campaigns = adminDataStore.campaigns.getAll();
      const index = campaigns.findIndex(c => c.id === id);
      if (index === -1) return null;
      campaigns[index] = { ...campaigns[index], ...data, updated_at: new Date().toISOString() };
      saveToStorage(STORAGE_KEYS.CAMPAIGNS, campaigns);
      return campaigns[index];
    },
  },

  // Evidences
  evidences: {
    getAll: (): AdminEvidence[] => {
      const { mockEvidences } = require('./mock-data/admin');
      return getFromStorage(STORAGE_KEYS.EVIDENCES, mockEvidences);
    },
    getById: (id: string): AdminEvidence | null => {
      const evidences = adminDataStore.evidences.getAll();
      return evidences.find(e => e.id === id) || null;
    },
    update: (id: string, data: Partial<AdminEvidence>): AdminEvidence | null => {
      const evidences = adminDataStore.evidences.getAll();
      const index = evidences.findIndex(e => e.id === id);
      if (index === -1) return null;
      evidences[index] = { ...evidences[index], ...data };
      saveToStorage(STORAGE_KEYS.EVIDENCES, evidences);
      return evidences[index];
    },
  },

  // Reports
  reports: {
    getAll: (): AdminReport[] => {
      const { mockReports } = require('./mock-data/admin');
      return getFromStorage(STORAGE_KEYS.REPORTS, mockReports);
    },
    getById: (id: string): AdminReport | null => {
      const reports = adminDataStore.reports.getAll();
      return reports.find(r => r.id === id) || null;
    },
    update: (id: string, data: Partial<AdminReport>): AdminReport | null => {
      const reports = adminDataStore.reports.getAll();
      const index = reports.findIndex(r => r.id === id);
      if (index === -1) return null;
      reports[index] = { ...reports[index], ...data, updated_at: new Date().toISOString() };
      saveToStorage(STORAGE_KEYS.REPORTS, reports);
      return reports[index];
    },
  },

  // Settings
  settings: {
    getAll: (): SystemSetting[] => {
      const { mockSystemSettings } = require('./mock-data/admin');
      return getFromStorage(STORAGE_KEYS.SETTINGS, mockSystemSettings);
    },
    getById: (id: string): SystemSetting | null => {
      const settings = adminDataStore.settings.getAll();
      return settings.find(s => s.id === id) || null;
    },
    update: (id: string, data: Partial<SystemSetting>): SystemSetting | null => {
      const settings = adminDataStore.settings.getAll();
      const index = settings.findIndex(s => s.id === id);
      if (index === -1) return null;
      settings[index] = { ...settings[index], ...data, updated_at: new Date().toISOString() };
      saveToStorage(STORAGE_KEYS.SETTINGS, settings);
      return settings[index];
    },
  },

  // Audit Logs
  auditLogs: {
    getAll: (): AuditLog[] => {
      const { mockAuditLogs } = require('./mock-data/admin');
      return getFromStorage(STORAGE_KEYS.AUDIT_LOGS, mockAuditLogs);
    },
    create: (log: Omit<AuditLog, 'id' | 'created_at'>): AuditLog => {
      const logs = adminDataStore.auditLogs.getAll();
      const newLog: AuditLog = {
        ...log,
        id: `log_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        created_at: new Date().toISOString(),
      };
      logs.unshift(newLog);
      saveToStorage(STORAGE_KEYS.AUDIT_LOGS, logs);
      return newLog;
    },
  },
};
