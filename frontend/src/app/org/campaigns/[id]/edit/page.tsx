"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import { Card, CardBody, CardHeader, Input, Textarea, Select, SelectItem, Button, Checkbox } from "@heroui/react";
import { getCampaignById, updateCampaign } from "@/lib/mockOrgData";
import type { Campaign } from "@/types/organization";

export default function EditCampaignPage() {
  const router = useRouter();
  const params = useParams();
  const [campaign, setCampaign] = useState<Campaign | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formData, setFormData] = useState<Partial<Campaign>>({});

  useEffect(() => {
    const id = params.id as string;
    const data = getCampaignById(id);
    if (data) {
      setCampaign(data);
      setFormData(data);
    } else {
      router.push('/org/campaigns');
    }
  }, [params.id, router]);

  const handleChange = (field: string, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    setErrors(prev => ({ ...prev, [field]: '' }));
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.title?.trim()) {
      newErrors.title = 'Title is required';
    }

    if (!formData.description?.trim()) {
      newErrors.description = 'Description is required';
    }

    if (!formData.category_id) {
      newErrors.category_id = 'Category is required';
    }

    const goalAmount = formData.goal_amount;
    if (!goalAmount || goalAmount <= 0) {
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

  const handleSubmit = async () => {
    if (!validate() || !campaign) return;

    setIsSaving(true);
    await new Promise(resolve => setTimeout(resolve, 500));

    updateCampaign(campaign.id, formData);
    setIsSaving(false);
    router.push('/org/campaigns');
  };

  if (!campaign) {
    return <div>Loading...</div>;
  }

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Edit Campaign</h1>
        <p className="text-gray-600 mt-1">Update campaign details</p>
      </div>

      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">Basic Information</h2>
        </CardHeader>
        <CardBody className="p-6 space-y-4">
          <Input
            label="Campaign Title"
            value={formData.title || ''}
            onChange={(e) => handleChange('title', e.target.value)}
            isRequired
            isInvalid={!!errors.title}
            errorMessage={errors.title}
          />

          <Select
            label="Category"
            selectedKeys={formData.category_id ? [formData.category_id] : []}
            onSelectionChange={(keys) => handleChange('category_id', Array.from(keys)[0])}
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
            value={formData.description || ''}
            onChange={(e) => handleChange('description', e.target.value)}
            minRows={2}
            isRequired
            isInvalid={!!errors.description}
            errorMessage={errors.description}
          />

          <Textarea
            label="Full Story (Optional)"
            value={formData.story || ''}
            onChange={(e) => handleChange('story', e.target.value)}
            minRows={5}
          />
        </CardBody>
      </Card>

      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">Financial Details</h2>
        </CardHeader>
        <CardBody className="p-6 space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="Goal Amount"
              type="number"
              value={String(formData.goal_amount || '')}
              onChange={(e) => handleChange('goal_amount', parseFloat(e.target.value))}
              isRequired
              isInvalid={!!errors.goal_amount}
              errorMessage={errors.goal_amount}
            />

            <div>
              <label className="text-sm font-medium text-gray-700 mb-2 block">Collected Amount</label>
              <p className="text-2xl font-bold text-green-600">₺{campaign.collected_amount.toLocaleString()}</p>
            </div>
          </div>
        </CardBody>
      </Card>

      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">Timeline & Status</h2>
        </CardHeader>
        <CardBody className="p-6 space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="Start Date"
              type="date"
              value={formData.start_date || ''}
              onChange={(e) => handleChange('start_date', e.target.value)}
              isRequired
              isInvalid={!!errors.start_date}
              errorMessage={errors.start_date}
            />

            <Input
              label="End Date"
              type="date"
              value={formData.end_date || ''}
              onChange={(e) => handleChange('end_date', e.target.value)}
              isRequired
              isInvalid={!!errors.end_date}
              errorMessage={errors.end_date}
            />
          </div>

          <Select
            label="Status"
            selectedKeys={formData.status ? [formData.status] : []}
            onSelectionChange={(keys) => handleChange('status', Array.from(keys)[0])}
          >
            <SelectItem key="draft">Draft</SelectItem>
            <SelectItem key="pending_approval">Pending Approval</SelectItem>
            <SelectItem key="active">Active</SelectItem>
            <SelectItem key="paused">Paused</SelectItem>
            <SelectItem key="completed">Completed</SelectItem>
            <SelectItem key="cancelled">Cancelled</SelectItem>
          </Select>
        </CardBody>
      </Card>

      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">Media</h2>
        </CardHeader>
        <CardBody className="p-6 space-y-4">
          <Input
            label="Image URL (Optional)"
            value={formData.image_url || ''}
            onChange={(e) => handleChange('image_url', e.target.value)}
            isInvalid={!!errors.image_url}
            errorMessage={errors.image_url}
          />

          <Input
            label="Video URL (Optional)"
            value={formData.video_url || ''}
            onChange={(e) => handleChange('video_url', e.target.value)}
            isInvalid={!!errors.video_url}
            errorMessage={errors.video_url}
          />
        </CardBody>
      </Card>

      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">Options</h2>
        </CardHeader>
        <CardBody className="p-6 space-y-3">
          <Checkbox
            isSelected={formData.is_featured || false}
            onValueChange={(checked) => handleChange('is_featured', checked)}
          >
            Featured Campaign
          </Checkbox>

          <Checkbox
            isSelected={formData.is_urgent || false}
            onValueChange={(checked) => handleChange('is_urgent', checked)}
          >
            Mark as Urgent
          </Checkbox>
        </CardBody>
      </Card>

      <div className="flex gap-4 justify-end pb-8">
        <Button variant="bordered" onClick={() => router.push('/org/campaigns')}>
          Cancel
        </Button>
        <Button color="primary" onClick={handleSubmit} isLoading={isSaving}>
          Save Changes
        </Button>
      </div>
    </div>
  );
}
