"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button, Progress } from "@heroui/react";
import OrganizationStep1, { type Step1Data } from "./OrganizationStep1";
import OrganizationStep2, { type Step2Data } from "./OrganizationStep2";
import { submitOrganizationRegistration, type OrganizationRegistrationPayload } from "@/lib/mockOrgRegistration";

const initialStep1: Step1Data = {
  email: '',
  password: '',
  confirmPassword: '',
  organization_type: '',
  legal_name: '',
  trade_name: '',
  website_url: '',
  logo_url: '',
  description: '',
  mission_statement: '',
  contact_email: '',
  contact_phone: '',
  country: 'Turkey',
  city: '',
  district: '',
  address_line1: '',
  address_line2: '',
};

const initialStep2: Step2Data = {
  bank_name: '',
  iban: '',
  currency: 'TRY',
  documents: [{
    document_type: '',
    document_name: '',
    file_url: '',
    expires_at: ''
  }],
};

export default function OrganizationSignupWizard() {
  const router = useRouter();
  const [currentStep, setCurrentStep] = useState(1);
  const [step1Data, setStep1Data] = useState<Step1Data>(initialStep1);
  const [step2Data, setStep2Data] = useState<Step2Data>(initialStep2);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validateStep1 = (): boolean => {
    const newErrors: Record<string, string> = {};

    // Email
    if (!step1Data.email) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(step1Data.email)) {
      newErrors.email = 'Invalid email format';
    }

    // Password
    if (!step1Data.password) {
      newErrors.password = 'Password is required';
    } else if (step1Data.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters';
    } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(step1Data.password)) {
      newErrors.password = 'Password must contain uppercase, lowercase, and number';
    }

    // Confirm Password
    if (!step1Data.confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password';
    } else if (step1Data.password !== step1Data.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    // Organization
    if (!step1Data.organization_type) {
      newErrors.organization_type = 'Organization type is required';
    }
    if (!step1Data.legal_name) {
      newErrors.legal_name = 'Legal name is required';
    }
    if (!step1Data.description) {
      newErrors.description = 'Description is required';
    }

    // Contact
    if (!step1Data.contact_email) {
      newErrors.contact_email = 'Contact email is required';
    } else if (!/\S+@\S+\.\S+/.test(step1Data.contact_email)) {
      newErrors.contact_email = 'Invalid email format';
    }
    if (!step1Data.contact_phone) {
      newErrors.contact_phone = 'Phone number is required';
    }
    if (!step1Data.country) {
      newErrors.country = 'Country is required';
    }
    if (!step1Data.city) {
      newErrors.city = 'City is required';
    }
    if (!step1Data.address_line1) {
      newErrors.address_line1 = 'Address is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const validateStep2 = (): boolean => {
    const newErrors: Record<string, string> = {};

    // Bank Account
    if (!step2Data.bank_name) {
      newErrors.bank_name = 'Bank name is required';
    }
    if (!step2Data.iban) {
      newErrors.iban = 'IBAN is required';
    } else {
      const cleanIban = step2Data.iban.replace(/\s/g, '');
      if (cleanIban.startsWith('TR') && cleanIban.length !== 26) {
        newErrors.iban = 'Turkish IBAN must be 26 characters';
      }
    }
    if (!step2Data.currency) {
      newErrors.currency = 'Currency is required';
    }

    // Documents
    if (step2Data.documents.length === 0) {
      newErrors.documents = 'At least one document is required';
    } else {
      step2Data.documents.forEach((doc, index) => {
        if (!doc.document_type) {
          newErrors[`documents.${index}.document_type`] = 'Document type is required';
        }
        if (!doc.document_name) {
          newErrors[`documents.${index}.document_name`] = 'Document name is required';
        }
        if (!doc.file_url) {
          newErrors[`documents.${index}.file_url`] = 'File URL is required';
        } else if (!/^https?:\/\/.+/.test(doc.file_url)) {
          newErrors[`documents.${index}.file_url`] = 'Must be a valid URL';
        }
      });
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleNext = () => {
    if (validateStep1()) {
      setCurrentStep(2);
      setErrors({});
    }
  };

  const handleBack = () => {
    setCurrentStep(1);
    setErrors({});
  };

  const handleSubmit = async () => {
    if (!validateStep2()) return;

    setIsSubmitting(true);

    const payload: OrganizationRegistrationPayload = {
      user: {
        email: step1Data.email,
        password: step1Data.password,
        role: step1Data.organization_type,
      },
      organization: {
        organization_type: step1Data.organization_type as 'foundation' | 'association' | 'ngo',
        legal_name: step1Data.legal_name,
        trade_name: step1Data.trade_name || undefined,
        website_url: step1Data.website_url || undefined,
        logo_url: step1Data.logo_url || undefined,
        description: step1Data.description,
        mission_statement: step1Data.mission_statement || undefined,
        verification_status: 'pending',
      },
      primaryContact: {
        contact_type: 'head_office',
        is_primary: true,
        email: step1Data.contact_email,
        phone: step1Data.contact_phone,
        country: step1Data.country,
        city: step1Data.city,
        district: step1Data.district || undefined,
        address_line1: step1Data.address_line1,
        address_line2: step1Data.address_line2 || undefined,
      },
      bankAccount: {
        bank_name: step2Data.bank_name,
        iban: step2Data.iban.replace(/\s/g, ''),
        currency: step2Data.currency,
        is_primary: true,
        is_verified: false,
      },
      documents: step2Data.documents.map(doc => ({
        document_type: doc.document_type,
        document_name: doc.document_name,
        file_url: doc.file_url,
        is_verified: false,
        expires_at: doc.expires_at || undefined,
      })),
    };

    const result = await submitOrganizationRegistration(payload);

    if (result.success) {
      router.push('/account/organization/pending');
    } else {
      setErrors({ submit: result.error || 'Failed to submit registration' });
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12">
      <div className="container mx-auto px-4 max-w-4xl">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Organization Registration
          </h1>
          <p className="text-gray-600">
            Step {currentStep} of 2: {currentStep === 1 ? 'Organization & Contact Details' : 'Bank & Documents'}
          </p>
        </div>

        {/* Progress */}
        <Progress
          value={(currentStep / 2) * 100}
          className="mb-8"
          color="primary"
        />

        {/* Step Content */}
        {currentStep === 1 && (
          <OrganizationStep1
            data={step1Data}
            onChange={setStep1Data}
            errors={errors}
          />
        )}

        {currentStep === 2 && (
          <OrganizationStep2
            data={step2Data}
            onChange={setStep2Data}
            errors={errors}
          />
        )}

        {/* Error Message */}
        {errors.submit && (
          <div className="mt-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-600">
            {errors.submit}
          </div>
        )}

        {/* Navigation */}
        <div className="mt-8 flex justify-between gap-4">
          {currentStep === 1 ? (
            <Button
              variant="bordered"
              onClick={() => router.push('/auth/register')}
            >
              Cancel
            </Button>
          ) : (
            <Button
              variant="bordered"
              onClick={handleBack}
              isDisabled={isSubmitting}
            >
              Back
            </Button>
          )}

          {currentStep === 1 ? (
            <Button
              color="primary"
              onClick={handleNext}
            >
              Next: Bank & Documents
            </Button>
          ) : (
            <Button
              color="primary"
              onClick={handleSubmit}
              isLoading={isSubmitting}
            >
              Submit Registration
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}
