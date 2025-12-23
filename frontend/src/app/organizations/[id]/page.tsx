"use client";

import { useParams } from "next/navigation";
import Link from "next/link";
import { Card, CardBody, CardHeader } from "@heroui/react";
import { Chip } from "@heroui/react";
import { Progress } from "@heroui/react";
import { Accordion, AccordionItem } from "@heroui/react";
import { Button } from "@heroui/button";
import CampaignCard from "@/components/ui/CampaignCard";
import { mockOrganizations, mockOrganizationContacts, mockOrganizationDocuments, mockOrganizationBankAccounts, mockTransparencyScores } from "@/lib/organizations";
import { mockCampaigns } from "@/lib/mockData";

export default function OrganizationProfilePage() {
  const params = useParams();
  const id = params.id as string;

  const organization = mockOrganizations.find(org => org.id === id);
  const contacts = mockOrganizationContacts.filter(c => c.organization_id === id);
  const primaryContact = contacts.find(c => c.is_primary);
  const documents = mockOrganizationDocuments.filter(d => d.organization_id === id);
  const bankAccounts = mockOrganizationBankAccounts.filter(b => b.organization_id === id);
  const transparencyScore = mockTransparencyScores.find(t => t.organization_id === id);
  const organizationCampaigns = mockCampaigns.filter(c => c.organizationId === id);
  const activeCampaigns = organizationCampaigns.filter(c => c.status === 'active');
  const otherCampaigns = organizationCampaigns.filter(c => c.status !== 'active');

  if (!organization) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="text-center">
          <div className="text-6xl mb-4">😔</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">
            Organization Not Found
          </h2>
          <p className="text-gray-600 mb-6">
            The organization you're looking for doesn't exist.
          </p>
          <Link href="/organizations">
            <Button color="primary">Browse Organizations</Button>
          </Link>
        </div>
      </div>
    );
  }

  const getVerificationColor = (status: string) => {
    switch (status) {
      case 'approved': return 'success';
      case 'in_review': return 'warning';
      case 'rejected': return 'danger';
      default: return 'default';
    }
  };

  const getOrganizationTypeColor = (type: string) => {
    switch (type) {
      case 'foundation': return 'primary';
      case 'association': return 'secondary';
      case 'ngo': return 'success';
      default: return 'default';
    }
  };

  const maskIBAN = (iban: string) => {
    if (iban.length <= 10) return iban;
    const first = iban.substring(0, 6);
    const last = iban.substring(iban.length - 4);
    return `${first}${'X'.repeat(iban.length - 10)}${last}`;
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        {/* Header Section */}
        <Card className="mb-8">
          <CardBody className="p-6">
            <div className="flex flex-col md:flex-row gap-6">
              {organization.logo_url && (
                <img
                  src={organization.logo_url}
                  alt={organization.legal_name}
                  className="w-32 h-32 rounded-lg object-cover flex-shrink-0"
                />
              )}
              
              <div className="flex-1">
                <div className="flex flex-wrap gap-2 mb-3">
                  <Chip 
                    color={getOrganizationTypeColor(organization.organization_type)}
                    variant="flat"
                  >
                    {organization.organization_type}
                  </Chip>
                  <Chip 
                    color={getVerificationColor(organization.verification_status)}
                  >
                    {organization.verification_status}
                  </Chip>
                  {organization.is_featured && (
                    <Chip color="warning" variant="flat">
                      ⭐ Featured
                    </Chip>
                  )}
                </div>

                <h1 className="text-3xl font-bold text-gray-900 mb-2">
                  {organization.legal_name}
                </h1>
                {organization.trade_name && (
                  <p className="text-lg text-gray-600 mb-3">
                    Trading as: {organization.trade_name}
                  </p>
                )}

                {organization.website_url && (
                  <a 
                    href={organization.website_url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-blue-600 hover:text-blue-800 inline-flex items-center gap-1"
                  >
                    🌐 {organization.website_url}
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                    </svg>
                  </a>
                )}

                <p className="text-gray-700 mt-4 mb-3">
                  {organization.description}
                </p>

                {organization.mission_statement && (
                  <div className="bg-blue-50 border-l-4 border-blue-500 p-4 rounded">
                    <p className="font-semibold text-blue-800 mb-1">Mission Statement</p>
                    <p className="text-blue-700">{organization.mission_statement}</p>
                  </div>
                )}
              </div>
            </div>
          </CardBody>
        </Card>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-8">
            {/* Transparency Section */}
            {transparencyScore && (
              <Card>
                <CardHeader className="pb-4">
                  <h2 className="text-2xl font-bold text-gray-900">Transparency Score</h2>
                </CardHeader>
                <CardBody className="p-6">
                  <div className="mb-6">
                    <div className="flex justify-between items-center mb-2">
                      <span className="text-lg font-semibold">Current Score</span>
                      <span className="text-3xl font-bold text-blue-600">{transparencyScore.current_score}%</span>
                    </div>
                    <Progress 
                      value={transparencyScore.current_score} 
                      color={transparencyScore.current_score >= 90 ? "success" : transparencyScore.current_score >= 75 ? "warning" : "default"}
                      className="h-3"
                    />
                    <p className="text-sm text-gray-500 mt-2">
                      Last calculated: {new Date(transparencyScore.last_calculated_at).toLocaleDateString()}
                    </p>
                  </div>

                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div className="text-center p-3 bg-gray-50 rounded-lg">
                      <p className="text-2xl font-bold text-gray-900">{transparencyScore.total_campaigns}</p>
                      <p className="text-sm text-gray-600">Total Campaigns</p>
                    </div>
                    <div className="text-center p-3 bg-gray-50 rounded-lg">
                      <p className="text-2xl font-bold text-green-600">{transparencyScore.completed_campaigns}</p>
                      <p className="text-sm text-gray-600">Completed</p>
                    </div>
                    <div className="text-center p-3 bg-gray-50 rounded-lg">
                      <p className="text-2xl font-bold text-blue-600">{transparencyScore.on_time_reports}</p>
                      <p className="text-sm text-gray-600">On-Time Reports</p>
                    </div>
                    <div className="text-center p-3 bg-gray-50 rounded-lg">
                      <p className="text-2xl font-bold text-orange-600">{transparencyScore.late_reports}</p>
                      <p className="text-sm text-gray-600">Late Reports</p>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4 mt-4">
                    <div className="text-center p-3 bg-green-50 rounded-lg border border-green-200">
                      <p className="text-2xl font-bold text-green-600">{transparencyScore.approved_evidences}</p>
                      <p className="text-sm text-green-700">Approved Evidences</p>
                    </div>
                    <div className="text-center p-3 bg-red-50 rounded-lg border border-red-200">
                      <p className="text-2xl font-bold text-red-600">{transparencyScore.rejected_evidences}</p>
                      <p className="text-sm text-red-700">Rejected Evidences</p>
                    </div>
                  </div>
                </CardBody>
              </Card>
            )}

            {/* Campaigns Section */}
            <div>
              <h2 className="text-2xl font-bold text-gray-900 mb-4">Campaigns</h2>
              
              {activeCampaigns.length > 0 && (
                <>
                  <h3 className="text-lg font-semibold text-gray-800 mb-3">Active Campaigns</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                    {activeCampaigns.map(campaign => (
                      <CampaignCard key={campaign.id} campaign={campaign} />
                    ))}
                  </div>
                </>
              )}

              {otherCampaigns.length > 0 && (
                <>
                  <h3 className="text-lg font-semibold text-gray-800 mb-3">Past Campaigns</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {otherCampaigns.map(campaign => (
                      <CampaignCard key={campaign.id} campaign={campaign} />
                    ))}
                  </div>
                </>
              )}

              {organizationCampaigns.length === 0 && (
                <Card>
                  <CardBody className="p-8 text-center">
                    <p className="text-gray-600">No campaigns yet</p>
                  </CardBody>
                </Card>
              )}
            </div>

            {/* Documents Section */}
            {documents.length > 0 && (
              <Card>
                <CardHeader className="pb-4">
                  <h2 className="text-2xl font-bold text-gray-900">Documents</h2>
                </CardHeader>
                <CardBody className="p-6">
                  <div className="space-y-3">
                    {documents.map(doc => (
                      <div key={doc.id} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-1">
                            <p className="font-semibold text-gray-900">{doc.document_name}</p>
                            {doc.is_verified && (
                              <Chip color="success" size="sm">Verified</Chip>
                            )}
                          </div>
                          <p className="text-sm text-gray-600">
                            Type: {doc.document_type} • Uploaded: {new Date(doc.uploaded_at).toLocaleDateString()}
                            {doc.expires_at && ` • Expires: ${new Date(doc.expires_at).toLocaleDateString()}`}
                          </p>
                        </div>
                        <a 
                          href={doc.file_url} 
                          target="_blank" 
                          rel="noopener noreferrer"
                          className="text-blue-600 hover:text-blue-800"
                        >
                          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                          </svg>
                        </a>
                      </div>
                    ))}
                  </div>
                </CardBody>
              </Card>
            )}
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Contact Information */}
            <Card>
              <CardHeader className="pb-4">
                <h2 className="text-xl font-bold text-gray-900">Contact & Location</h2>
              </CardHeader>
              <CardBody className="p-6">
                {primaryContact ? (
                  <div className="space-y-3">
                    {primaryContact.email && (
                      <div className="flex items-start gap-2">
                        <span className="text-gray-500">📧</span>
                        <div>
                          <p className="text-sm text-gray-600">Email</p>
                          <a href={`mailto:${primaryContact.email}`} className="text-blue-600 hover:text-blue-800">
                            {primaryContact.email}
                          </a>
                        </div>
                      </div>
                    )}
                    {primaryContact.phone && (
                      <div className="flex items-start gap-2">
                        <span className="text-gray-500">📞</span>
                        <div>
                          <p className="text-sm text-gray-600">Phone</p>
                          <a href={`tel:${primaryContact.phone}`} className="text-blue-600 hover:text-blue-800">
                            {primaryContact.phone}
                          </a>
                        </div>
                      </div>
                    )}
                    {(primaryContact.address_line1 || primaryContact.city) && (
                      <div className="flex items-start gap-2">
                        <span className="text-gray-500">📍</span>
                        <div>
                          <p className="text-sm text-gray-600">Address</p>
                          <div className="text-gray-900">
                            {primaryContact.address_line1 && <p>{primaryContact.address_line1}</p>}
                            {primaryContact.address_line2 && <p>{primaryContact.address_line2}</p>}
                            <p>
                              {primaryContact.city && `${primaryContact.city}`}
                              {primaryContact.district && `, ${primaryContact.district}`}
                            </p>
                            {primaryContact.postal_code && <p>{primaryContact.postal_code}</p>}
                            <p>{primaryContact.country}</p>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                ) : (
                  <p className="text-gray-600">No contact information available</p>
                )}

                {contacts.length > 1 && (
                  <Accordion className="mt-4">
                    <AccordionItem key="other-contacts" title={`${contacts.length - 1} other contact(s)`}>
                      {contacts.filter(c => !c.is_primary).map(contact => (
                        <div key={contact.id} className="mb-4 pb-4 border-b last:border-b-0">
                          <p className="font-semibold text-gray-900 mb-2">
                            {contact.contact_type}
                          </p>
                          {contact.email && <p className="text-sm text-gray-600">{contact.email}</p>}
                          {contact.phone && <p className="text-sm text-gray-600">{contact.phone}</p>}
                        </div>
                      ))}
                    </AccordionItem>
                  </Accordion>
                )}
              </CardBody>
            </Card>

            {/* Bank Accounts */}
            {bankAccounts.length > 0 && (
              <Card>
                <CardHeader className="pb-4">
                  <h2 className="text-xl font-bold text-gray-900">Bank Accounts</h2>
                </CardHeader>
                <CardBody className="p-6">
                  <div className="space-y-4">
                    {bankAccounts.map(account => (
                      <div key={account.id} className="p-4 bg-gray-50 rounded-lg">
                        <div className="flex items-center justify-between mb-2">
                          <p className="font-semibold text-gray-900">{account.bank_name}</p>
                          <div className="flex gap-1">
                            {account.is_primary && <Chip color="primary" size="sm">Primary</Chip>}
                            {account.is_verified && <Chip color="success" size="sm">Verified</Chip>}
                          </div>
                        </div>
                        <p className="text-sm text-gray-600">
                          IBAN: {maskIBAN(account.iban)}
                        </p>
                        <p className="text-sm text-gray-600">
                          Currency: {account.currency}
                        </p>
                      </div>
                    ))}
                  </div>
                </CardBody>
              </Card>
            )}

            {/* Metadata */}
            <Card>
              <CardHeader className="pb-4">
                <h2 className="text-xl font-bold text-gray-900">Organization Info</h2>
              </CardHeader>
              <CardBody className="p-6">
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Member since:</span>
                    <span className="font-semibold text-gray-900">
                      {new Date(organization.created_at).toLocaleDateString()}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Last updated:</span>
                    <span className="font-semibold text-gray-900">
                      {new Date(organization.updated_at).toLocaleDateString()}
                    </span>
                  </div>
                </div>
              </CardBody>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
