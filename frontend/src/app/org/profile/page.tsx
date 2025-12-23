"use client";

import { useEffect, useState } from "react";
import { Card, CardBody, CardHeader, Input, Textarea, Select, SelectItem, Button, Chip } from "@heroui/react";
import { getOrganization, updateOrganization } from "@/lib/mockOrgData";
import type { Organization } from "@/types/organization";

export default function OrgProfilePage() {
  const [org, setOrg] = useState<Organization | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [formData, setFormData] = useState<Partial<Organization>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    const data = getOrganization();
    setOrg(data);
    setFormData(data);
  }, []);

  const handleChange = (field: keyof Organization, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    setErrors(prev => ({ ...prev, [field]: '' }));
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.legal_name?.trim()) {
      newErrors.legal_name = 'Legal name is required';
    }

    if (!formData.description?.trim()) {
      newErrors.description = 'Description is required';
    }

    if (formData.website_url && !/^https?:\/\/.+/.test(formData.website_url)) {
      newErrors.website_url = 'Must be a valid URL';
    }

    if (formData.logo_url && !/^https?:\/\/.+/.test(formData.logo_url)) {
      newErrors.logo_url = 'Must be a valid URL';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = async () => {
    if (!validate()) return;

    setIsSaving(true);
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 500));

    const updated = updateOrganization(formData);
    setOrg(updated);
    setFormData(updated);
    setIsEditing(false);
    setIsSaving(false);
  };

  const handleCancel = () => {
    if (org) {
      setFormData(org);
    }
    setErrors({});
    setIsEditing(false);
  };

  if (!org) {
    return <div>Loading...</div>;
  }

  const getVerificationColor = (status: string) => {
    switch (status) {
      case 'approved': return 'success';
      case 'in_review': return 'warning';
      case 'rejected': return 'danger';
      default: return 'default';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">Organization Profile</h1>
        {!isEditing ? (
          <Button color="primary" onClick={() => setIsEditing(true)}>
            Edit Profile
          </Button>
        ) : (
          <div className="flex gap-2">
            <Button variant="bordered" onClick={handleCancel}>
              Cancel
            </Button>
            <Button color="primary" onClick={handleSave} isLoading={isSaving}>
              Save Changes
            </Button>
          </div>
        )}
      </div>

      {/* Basic Information */}
      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">Basic Information</h2>
        </CardHeader>
        <CardBody className="p-6 space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Select
              label="Organization Type"
              selectedKeys={formData.organization_type ? [formData.organization_type] : []}
              onSelectionChange={(keys) => handleChange('organization_type', Array.from(keys)[0] as string)}
              isDisabled={!isEditing}
            >
              <SelectItem key="foundation">Foundation</SelectItem>
              <SelectItem key="association">Association</SelectItem>
              <SelectItem key="ngo">NGO</SelectItem>
            </Select>

            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-gray-700">Verification Status</label>
              <Chip color={getVerificationColor(org.verification_status)} variant="flat" size="lg">
                {org.verification_status.replace('_', ' ').toUpperCase()}
              </Chip>
            </div>
          </div>

          <Input
            label="Legal Name"
            value={formData.legal_name || ''}
            onChange={(e) => handleChange('legal_name', e.target.value)}
            isRequired
            isDisabled={!isEditing}
            isInvalid={!!errors.legal_name}
            errorMessage={errors.legal_name}
          />

          <Input
            label="Trade Name (Optional)"
            value={formData.trade_name || ''}
            onChange={(e) => handleChange('trade_name', e.target.value)}
            isDisabled={!isEditing}
          />

          <Textarea
            label="Description"
            value={formData.description || ''}
            onChange={(e) => handleChange('description', e.target.value)}
            minRows={3}
            isRequired
            isDisabled={!isEditing}
            isInvalid={!!errors.description}
            errorMessage={errors.description}
          />

          <Textarea
            label="Mission Statement (Optional)"
            value={formData.mission_statement || ''}
            onChange={(e) => handleChange('mission_statement', e.target.value)}
            minRows={3}
            isDisabled={!isEditing}
          />
        </CardBody>
      </Card>

      {/* Online Presence */}
      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">Online Presence</h2>
        </CardHeader>
        <CardBody className="p-6 space-y-4">
          <Input
            label="Website URL (Optional)"
            value={formData.website_url || ''}
            onChange={(e) => handleChange('website_url', e.target.value)}
            placeholder="https://www.example.org"
            isDisabled={!isEditing}
            isInvalid={!!errors.website_url}
            errorMessage={errors.website_url}
          />

          <Input
            label="Logo URL (Optional)"
            value={formData.logo_url || ''}
            onChange={(e) => handleChange('logo_url', e.target.value)}
            placeholder="https://example.com/logo.png"
            isDisabled={!isEditing}
            isInvalid={!!errors.logo_url}
            errorMessage={errors.logo_url}
          />

          {formData.logo_url && (
            <div className="flex items-center gap-4">
              <span className="text-sm text-gray-600">Preview:</span>
              <img
                src={formData.logo_url}
                alt="Logo preview"
                className="h-16 w-16 object-contain border border-gray-200 rounded"
              />
            </div>
          )}
        </CardBody>
      </Card>

      {/* Metadata */}
      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">Metadata</h2>
        </CardHeader>
        <CardBody className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-gray-600">Created At</p>
              <p className="font-medium mt-1">
                {new Date(org.created_at).toLocaleDateString('en-US', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                })}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Last Updated</p>
              <p className="font-medium mt-1">
                {new Date(org.updated_at).toLocaleDateString('en-US', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                })}
              </p>
            </div>
          </div>
        </CardBody>
      </Card>
    </div>
  );
}
