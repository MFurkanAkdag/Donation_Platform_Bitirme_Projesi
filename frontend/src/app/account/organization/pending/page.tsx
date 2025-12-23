"use client";

import { useEffect, useState } from "react";
import { Card, CardBody, Chip, Divider } from "@heroui/react";
import { Button } from "@heroui/button";
import { useRouter } from "next/navigation";
import { getOrganizationRegistration, type OrganizationRegistrationPayload } from "@/lib/mockOrgRegistration";

export default function OrganizationPendingPage() {
  const router = useRouter();
  const [registration, setRegistration] = useState<OrganizationRegistrationPayload | null>(null);

  useEffect(() => {
    const data = getOrganizationRegistration();
    if (!data) {
      // Redirect if no registration found
      router.push('/auth/register');
    } else {
      setRegistration(data);
    }
  }, [router]);

  if (!registration) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12">
      <div className="container mx-auto px-4 max-w-3xl">
        {/* Success Header */}
        <div className="text-center mb-8">
          <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-blue-100 mb-4">
            <svg 
              className="h-10 w-10 text-blue-600" 
              fill="none" 
              viewBox="0 0 24 24" 
              stroke="currentColor"
            >
              <path 
                strokeLinecap="round" 
                strokeLinejoin="round" 
                strokeWidth={2} 
                d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" 
              />
            </svg>
          </div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Registration Submitted Successfully
          </h1>
          <p className="text-gray-600">
            Your organization registration is pending review by our team
          </p>
        </div>

        {/* Status Card */}
        <Card className="mb-6">
          <CardBody className="p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-bold text-gray-900">Verification Status</h2>
              <Chip color="warning" variant="flat">
                Pending Review
              </Chip>
            </div>
            <Divider className="my-4" />
            <p className="text-gray-600 mb-4">
              Our team will review your submission within 3-5 business days. You will receive an email notification once the review is complete.
            </p>
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <h3 className="font-semibold text-blue-900 mb-2">What happens next?</h3>
              <ul className="space-y-2 text-sm text-blue-800">
                <li className="flex items-start">
                  <span className="mr-2">•</span>
                  <span>We'll verify your organization's legal documents</span>
                </li>
                <li className="flex items-start">
                  <span className="mr-2">•</span>
                  <span>Bank account information will be validated</span>
                </li>
                <li className="flex items-start">
                  <span className="mr-2">•</span>
                  <span>Your contact details will be confirmed</span>
                </li>
                <li className="flex items-start">
                  <span className="mr-2">•</span>
                  <span>You'll receive approval or requests for additional information</span>
                </li>
              </ul>
            </div>
          </CardBody>
        </Card>

        {/* Registration Summary */}
        <Card>
          <CardBody className="p-6">
            <h2 className="text-xl font-bold text-gray-900 mb-4">Registration Summary</h2>
            
            <div className="space-y-4">
              {/* Organization Info */}
              <div>
                <h3 className="font-semibold text-gray-700 mb-2">Organization</h3>
                <div className="bg-gray-50 rounded-lg p-4 space-y-2">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Legal Name:</span>
                    <span className="font-medium">{registration.organization.legal_name}</span>
                  </div>
                  {registration.organization.trade_name && (
                    <div className="flex justify-between">
                      <span className="text-gray-600">Trade Name:</span>
                      <span className="font-medium">{registration.organization.trade_name}</span>
                    </div>
                  )}
                  <div className="flex justify-between">
                    <span className="text-gray-600">Type:</span>
                    <Chip size="sm" variant="flat">
                      {registration.organization.organization_type.toUpperCase()}
                    </Chip>
                  </div>
                </div>
              </div>

              <Divider />

              {/* Contact Info */}
              <div>
                <h3 className="font-semibold text-gray-700 mb-2">Primary Contact</h3>
                <div className="bg-gray-50 rounded-lg p-4 space-y-2">
                  <div className="flex justify-between">
                    <span className="text-gray-600">City:</span>
                    <span className="font-medium">{registration.primaryContact.city}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Email:</span>
                    <span className="font-medium">{registration.primaryContact.email}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Phone:</span>
                    <span className="font-medium">{registration.primaryContact.phone}</span>
                  </div>
                </div>
              </div>

              <Divider />

              {/* Bank Info */}
              <div>
                <h3 className="font-semibold text-gray-700 mb-2">Bank Account</h3>
                <div className="bg-gray-50 rounded-lg p-4 space-y-2">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Bank:</span>
                    <span className="font-medium">{registration.bankAccount.bank_name}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Currency:</span>
                    <span className="font-medium">{registration.bankAccount.currency}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">Verification:</span>
                    <Chip size="sm" color="warning" variant="flat">
                      Pending
                    </Chip>
                  </div>
                </div>
              </div>

              <Divider />

              {/* Documents */}
              <div>
                <h3 className="font-semibold text-gray-700 mb-2">Documents Submitted</h3>
                <div className="bg-gray-50 rounded-lg p-4">
                  <div className="flex justify-between items-center">
                    <span className="text-gray-600">Total Documents:</span>
                    <Chip size="sm" color="primary" variant="flat">
                      {registration.documents.length} {registration.documents.length === 1 ? 'Document' : 'Documents'}
                    </Chip>
                  </div>
                  <div className="mt-3 space-y-2">
                    {registration.documents.map((doc, index) => (
                      <div key={index} className="flex items-center justify-between text-sm">
                        <span className="text-gray-700">{doc.document_name}</span>
                        <Chip size="sm" variant="flat">
                          {doc.document_type.replace(/_/g, ' ')}
                        </Chip>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </CardBody>
        </Card>

        {/* Actions */}
        <div className="mt-8 flex gap-4 justify-center">
          <Button
            variant="bordered"
            onClick={() => router.push('/')}
          >
            Return to Home
          </Button>
          <Button
            color="primary"
            onClick={() => router.push('/account')}
          >
            Go to Dashboard
          </Button>
        </div>
      </div>
    </div>
  );
}
