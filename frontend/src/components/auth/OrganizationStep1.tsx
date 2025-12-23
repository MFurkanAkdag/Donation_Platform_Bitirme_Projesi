"use client";

import { useState } from "react";
import { Card, CardBody } from "@heroui/react";
import { Input, Textarea, Select, SelectItem } from "@heroui/react";

interface Step1Data {
  // Account
  email: string;
  password: string;
  confirmPassword: string;
  // Organization
  organization_type: string;
  legal_name: string;
  trade_name: string;
  website_url: string;
  logo_url: string;
  description: string;
  mission_statement: string;
  // Primary Contact
  contact_email: string;
  contact_phone: string;
  country: string;
  city: string;
  district: string;
  address_line1: string;
  address_line2: string;
}

interface OrganizationStep1Props {
  data: Step1Data;
  onChange: (data: Step1Data) => void;
  errors: Record<string, string>;
}

export default function OrganizationStep1({ data, onChange, errors }: OrganizationStep1Props) {
  const handleChange = (field: keyof Step1Data, value: string) => {
    onChange({ ...data, [field]: value });
  };

  return (
    <div className="space-y-6">
      {/* Account Information */}
      <Card>
        <CardBody className="p-6">
          <h3 className="text-xl font-bold text-gray-900 mb-4">Account Information</h3>
          <div className="space-y-4">
            <Input
              label="Email Address"
              type="email"
              value={data.email}
              onChange={(e) => handleChange('email', e.target.value)}
              placeholder="organization@example.com"
              isRequired
              isInvalid={!!errors.email}
              errorMessage={errors.email}
            />
            <Input
              label="Password"
              type="password"
              value={data.password}
              onChange={(e) => handleChange('password', e.target.value)}
              placeholder="••••••••"
              isRequired
              isInvalid={!!errors.password}
              errorMessage={errors.password}
              description="At least 8 characters with uppercase, lowercase, and number"
            />
            <Input
              label="Confirm Password"
              type="password"
              value={data.confirmPassword}
              onChange={(e) => handleChange('confirmPassword', e.target.value)}
              placeholder="••••••••"
              isRequired
              isInvalid={!!errors.confirmPassword}
              errorMessage={errors.confirmPassword}
            />
          </div>
        </CardBody>
      </Card>

      {/* Organization Details */}
      <Card>
        <CardBody className="p-6">
          <h3 className="text-xl font-bold text-gray-900 mb-4">Organization Details</h3>
          <div className="space-y-4">
            <Select
              label="Organization Type"
              selectedKeys={data.organization_type ? [data.organization_type] : []}
              onSelectionChange={(keys) => handleChange('organization_type', Array.from(keys)[0] as string)}
              isRequired
              isInvalid={!!errors.organization_type}
              errorMessage={errors.organization_type}
            >
              <SelectItem key="foundation">Foundation</SelectItem>
              <SelectItem key="association">Association</SelectItem>
              <SelectItem key="ngo">NGO</SelectItem>
            </Select>

            <Input
              label="Legal Name"
              value={data.legal_name}
              onChange={(e) => handleChange('legal_name', e.target.value)}
              placeholder="Official registered name"
              isRequired
              isInvalid={!!errors.legal_name}
              errorMessage={errors.legal_name}
            />

            <Input
              label="Trade Name (Optional)"
              value={data.trade_name}
              onChange={(e) => handleChange('trade_name', e.target.value)}
              placeholder="Common name or brand"
            />

            <Input
              label="Website URL (Optional)"
              value={data.website_url}
              onChange={(e) => handleChange('website_url', e.target.value)}
              placeholder="https://www.example.org"
            />

            <Input
              label="Logo URL (Optional)"
              value={data.logo_url}
              onChange={(e) => handleChange('logo_url', e.target.value)}
              placeholder="https://example.com/logo.png"
            />

            <Textarea
              label="Description"
              value={data.description}
              onChange={(e) => handleChange('description', e.target.value)}
              placeholder="Brief description of your organization"
              minRows={3}
              isRequired
              isInvalid={!!errors.description}
              errorMessage={errors.description}
            />

            <Textarea
              label="Mission Statement (Optional)"
              value={data.mission_statement}
              onChange={(e) => handleChange('mission_statement', e.target.value)}
              placeholder="Your organization's mission and goals"
              minRows={2}
            />
          </div>
        </CardBody>
      </Card>

      {/* Primary Contact */}
      <Card>
        <CardBody className="p-6">
          <h3 className="text-xl font-bold text-gray-900 mb-4">Primary Contact Information</h3>
          <div className="space-y-4">
            <Input
              label="Contact Email"
              type="email"
              value={data.contact_email}
              onChange={(e) => handleChange('contact_email', e.target.value)}
              placeholder="contact@organization.com"
              isRequired
              isInvalid={!!errors.contact_email}
              errorMessage={errors.contact_email}
            />

            <Input
              label="Phone Number"
              type="tel"
              value={data.contact_phone}
              onChange={(e) => handleChange('contact_phone', e.target.value)}
              placeholder="+90 555 123 4567"
              isRequired
              isInvalid={!!errors.contact_phone}
              errorMessage={errors.contact_phone}
            />

            <div className="grid grid-cols-2 gap-4">
              <Input
                label="Country"
                value={data.country}
                onChange={(e) => handleChange('country', e.target.value)}
                placeholder="Turkey"
                isRequired
                isInvalid={!!errors.country}
                errorMessage={errors.country}
              />

              <Input
                label="City"
                value={data.city}
                onChange={(e) => handleChange('city', e.target.value)}
                placeholder="Istanbul"
                isRequired
                isInvalid={!!errors.city}
                errorMessage={errors.city}
              />
            </div>

            <Input
              label="District (Optional)"
              value={data.district}
              onChange={(e) => handleChange('district', e.target.value)}
              placeholder="Kadıköy"
            />

            <Input
              label="Address Line 1"
              value={data.address_line1}
              onChange={(e) => handleChange('address_line1', e.target.value)}
              placeholder="Street address"
              isRequired
              isInvalid={!!errors.address_line1}
              errorMessage={errors.address_line1}
            />

            <Input
              label="Address Line 2 (Optional)"
              value={data.address_line2}
              onChange={(e) => handleChange('address_line2', e.target.value)}
              placeholder="Apartment, suite, etc."
            />
          </div>
        </CardBody>
      </Card>
    </div>
  );
}

export type { Step1Data };
