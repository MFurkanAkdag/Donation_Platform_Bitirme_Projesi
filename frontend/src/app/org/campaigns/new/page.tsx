"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Card, CardBody, CardHeader, Input, Textarea, Select, SelectItem, Button, Checkbox } from "@heroui/react";
import { addCampaign } from "@/lib/mockOrgData";

export default function NewCampaignPage() {
  const router = useRouter();
  const [isSaving, setIsSaving] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formData, setFormData] = useState({
    organization_id: 'org_1',
    category_id: '',
    title: '',
    description: '',
    story: '',
    goal_amount: '',
    currency: 'TRY',
    status: 'draft',
    start_date: '',
    end_date: '',
    image_url: '',
    video_url: '',
    is_featured: false,
    is_urgent: false,
  });

  const handleChange = (field: string, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    setErrors(prev => ({ ...prev, [field]: '' }));
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.title.trim()) {
      newErrors.title = 'Title is required';
    }

    if (!formData.description.trim()) {
      newErrors.description = 'Description is required';
    }

    if (!formData.category_id) {
      newErrors.category_id = 'Category is required';
    }

    const goalAmount = parseFloat(formData.goal_amount);
    if (!formData.goal_amount || isNaN(goalAmount) || goalAmount <= 0) {
      newErrors.goal_amount = 'Valid goal amount is required';
    }

    if (!formData.start_date) {
      newErrors.start_date = 'Start date is required';
    }

    if (!formData.end_date) {
      newErrors.end_date = 'End date is required';
    }

    if (formData.start_date && formData.end_date && formData.start_date >= formData.end_date) {
      newErrors.end_date = 'End date must be after start date';
    }

    if (formData.image_url && !/^https?:\/\/.+/.test(formData.image_url)) {
      newErrors.image_url = 'Must be a valid URL';
    }

    if (formData.video_url && !/^https?:\/\/.+/.test(formData.video_url)) {
      newErrors.video_url = 'Must be a valid URL';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (status: 'draft' | 'pending_approval') => {
    if (!validate()) return;

    setIsSaving(true);
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 500));

    const campaign = addCampaign({
      organization_id: formData.organization_id,
      category_id: formData.category_id,
      title: formData.title,
      description: formData.description,
      story: formData.story || undefined,
      goal_amount: parseFloat(formData.goal_amount),
      collected_amount: 0,
      currency: formData.currency,
      status,
      start_date: formData.start_date,
      end_date: formData.end_date,
      image_url: formData.image_url || undefined,
      video_url: formData.video_url || undefined,
      is_featured: formData.is_featured,
      is_urgent: formData.is_urgent,
    });

    setIsSaving(false);
    router.push('/org/campaigns');
  };

  return (
    <div className="space-y-6 max-w-4xl">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Create New Campaign</h1>
        <p className="text-gray-600 mt-1">Fill in the details to create a new fundraising campaign</p>
      </div>

      {/* Basic Information */}
      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">Basic Information</h2>
        </CardHeader>
        <CardBody className="p-6 space-y-4">
          <Input
            label="Campaign Title"
            value={formData.title}
            onChange={(e) => handleChange('title', e.target.value)}
            placeholder="e.g., Books for Rural Schools"
            isRequired
            isInvalid={!!errors.title}
            errorMessage={errors.title}
          />

          <Select
            label="Category"
            selectedKeys={formData.category_id ? [formData.category_id] : []}
            onSelectionChange={(keys) => handleChange('category_id', Array.from(keys)[0] as string)}
            isRequired
            isInvalid={!!errors.category_id}
            errorMessage={errors.category_id}
          >
            <SelectItem key="cat_education">Education</SelectItem>
            <SelectItem key="cat_health">Health</SelectItem>
            <SelectItem key="cat_environment">Environment</SelectItem>
            <SelectItem key="cat_animals">Animal Welfare</SelectItem>
            <SelectItem key="cat_disaster">Disaster Relief</SelectItem>
            <SelectItem key="cat_other">Other</SelectItem>
          </Select>

          <Textarea
            label="Short Description"
            value={formData.description}
            onChange={(e) => handleChange('description', e.target.value)}
            placeholder="Brief description shown on campaign cards"
            minRows={2}
            isRequired
            isInvalid={!!errors.description}
            errorMessage={errors.description}
          />

          <Textarea
            label="Full Story (Optional)"
            value={formData.story}
            onChange={(e) => handleChange('story', e.target.value)}
            placeholder="Detailed story about the campaign and its impact"
            minRows={5}
          />
        </CardBody>
      </Card>

      {/* Financial Details */}
      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">Financial Details</h2>
        </CardHeader>
        <CardBody className="p-6 space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="Goal Amount"
              type="number"
              value={formData.goal_amount}
              onChange={(e) => handleChange('goal_amount', e.target.value)}
              placeholder="50000"
              isRequired
              isInvalid={!!errors.goal_amount}
              errorMessage={errors.goal_amount}
            />

            <Select
              label="Currency"
              selectedKeys={formData.currency ? [formData.currency] : []}
              onSelectionChange={(keys) => handleChange('currency', Array.from(keys)[0] as string)}
              isRequired
            >
              <SelectItem key="TRY">Turkish Lira (TRY)</SelectItem>
              <SelectItem key="USD">US Dollar (USD)</SelectItem>
              <SelectItem key="EUR">Euro (EUR)</SelectItem>
            </Select>
          </div>
        </CardBody>
      </Card>

      {/* Timeline */}
      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">Timeline</h2>
        </CardHeader>
        <CardBody className="p-6 space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="Start Date"
              type="date"
              value={formData.start_date}
              onChange={(e) => handleChange('start_date', e.target.value)}
              isRequired
              isInvalid={!!errors.start_date}
              errorMessage={errors.start_date}
            />

            <Input
              label="End Date"
              type="date"
              value={formData.end_date}
              onChange={(e) => handleChange('end_date', e.target.value)}
              isRequired
              isInvalid={!!errors.end_date}
              errorMessage={errors.end_date}
            />
          </div>
        </CardBody>
      </Card>

      {/* Media */}
      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">Media</h2>
        </CardHeader>
        <CardBody className="p-6 space-y-4">
          <Input
            label="Image URL (Optional)"
            value={formData.image_url}
            onChange={(e) => handleChange('image_url', e.target.value)}
            placeholder="https://example.com/image.jpg"
            isInvalid={!!errors.image_url}
            errorMessage={errors.image_url}
          />

          <Input
            label="Video URL (Optional)"
            value={formData.video_url}
            onChange={(e) => handleChange('video_url', e.target.value)}
            placeholder="https://youtube.com/watch?v=..."
            isInvalid={!!errors.video_url}
            errorMessage={errors.video_url}
          />
        </CardBody>
      </Card>

      {/* Options */}
      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">Options</h2>
        </CardHeader>
        <CardBody className="p-6 space-y-3">
          <Checkbox
            isSelected={formData.is_featured}
            onValueChange={(checked) => handleChange('is_featured', checked)}
          >
            Featured Campaign
          </Checkbox>

          <Checkbox
            isSelected={formData.is_urgent}
            onValueChange={(checked) => handleChange('is_urgent', checked)}
          >
            Mark as Urgent
          </Checkbox>
        </CardBody>
      </Card>

      {/* Actions */}
      <div className="flex gap-4 justify-end pb-8">
        <Button variant="bordered" onClick={() => router.push('/org/campaigns')}>
          Cancel
        </Button>
        <Button
          variant="flat"
          onClick={() => handleSubmit('draft')}
          isLoading={isSaving}
        >
          Save as Draft
        </Button>
        <Button
          color="primary"
          onClick={() => handleSubmit('pending_approval')}
          isLoading={isSaving}
        >
          Submit for Approval
        </Button>
      </div>
    </div>
  );
}
