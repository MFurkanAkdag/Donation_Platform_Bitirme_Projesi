"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Card, CardBody, CardHeader, Chip, Button } from "@heroui/react";
import { useAuth } from "@/contexts/AuthContext";
import { adminDataStore } from "@/lib/adminDataStore";
import type { AdminCampaign } from "@/types/admin";

export default function CampaignDetailPage() {
  const params = useParams();
  const { user } = useAuth();
  const [campaign, setCampaign] = useState<AdminCampaign | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const id = params.id as string;
    const data = adminDataStore.campaigns.getById(id);
    setCampaign(data);
  }, [params.id]);

  const handleApprove = async () => {
    if (!campaign || !user) return;
    setIsLoading(true);

    const updated = adminDataStore.campaigns.update(campaign.id, { status: 'active' });

    adminDataStore.auditLogs.create({
      user_id: user.id,
      action: 'campaign.approved',
      entity_type: 'campaign',
      entity_id: campaign.id,
      old_values: { status: campaign.status },
      new_values: { status: 'active' },
    });

    setCampaign(updated);
    setIsLoading(false);
  };

  const handlePause = async () => {
    if (!campaign || !user) return;
    setIsLoading(true);

    const updated = adminDataStore.campaigns.update(campaign.id, { status: 'paused' });

    adminDataStore.auditLogs.create({
      user_id: user.id,
      action: 'campaign.paused',
      entity_type: 'campaign',
      entity_id: campaign.id,
      old_values: { status: campaign.status },
      new_values: { status: 'paused' },
    });

    setCampaign(updated);
    setIsLoading(false);
  };

  const handleResume = async () => {
    if (!campaign || !user) return;
    setIsLoading(true);

    const updated = adminDataStore.campaigns.update(campaign.id, { status: 'active' });

    adminDataStore.auditLogs.create({
      user_id: user.id,
      action: 'campaign.resumed',
      entity_type: 'campaign',
      entity_id: campaign.id,
      old_values: { status: campaign.status },
      new_values: { status: 'active' },
    });

    setCampaign(updated);
    setIsLoading(false);
  };

  const toggleFeatured = async () => {
    if (!campaign || !user) return;
    setIsLoading(true);

    const updated = adminDataStore.campaigns.update(campaign.id, { is_featured: !campaign.is_featured });

    adminDataStore.auditLogs.create({
      user_id: user.id,
      action: campaign.is_featured ? 'campaign.unfeatured' : 'campaign.featured',
      entity_type: 'campaign',
      entity_id: campaign.id,
      old_values: { is_featured: campaign.is_featured },
      new_values: { is_featured: !campaign.is_featured },
    });

    setCampaign(updated);
    setIsLoading(false);
  };

  if (!campaign) return <div>Loading...</div>;

  const getStatusColor = (status: string) => {
    const colors: Record<string, any> = {
      active: 'success', completed: 'default', pending_approval: 'warning',
      draft: 'default', paused: 'warning', cancelled: 'danger'
    };
    return colors[status] || 'default';
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">{campaign.title}</h1>
          <Chip color={getStatusColor(campaign.status)} variant="flat" className="mt-2">
            {campaign.status.replace('_', ' ').toUpperCase()}
          </Chip>
        </div>
      </div>

      <Card>
        <CardBody className="p-4">
          <div className="flex flex-wrap gap-3">
            {campaign.status === 'pending_approval' && (
              <Button color="success" onClick={handleApprove} isLoading={isLoading}>
                Approve Campaign
              </Button>
            )}
            {campaign.status === 'active' && (
              <Button color="warning" onClick={handlePause} isLoading={isLoading}>
                Pause Campaign
              </Button>
            )}
            {campaign.status === 'paused' && (
              <Button color="success" onClick={handleResume} isLoading={isLoading}>
                Resume Campaign
              </Button>
            )}
            <Button variant="bordered" onClick={toggleFeatured} isLoading={isLoading}>
              {campaign.is_featured ? 'Remove from Featured' : 'Add to Featured'}
            </Button>
          </div>
        </CardBody>
      </Card>

      <Card>
        <CardHeader className="pt-6 px-6"><h2 className="text-xl font-bold">Campaign Details</h2></CardHeader>
        <CardBody className="p-6">
          <div className="grid grid-cols-2 gap-4">
            <div><p className="text-sm text-gray-600">Goal Amount</p><p className="font-medium">₺{campaign.goal_amount.toLocaleString()}</p></div>
            <div><p className="text-sm text-gray-600">Collected</p><p className="font-medium">₺{campaign.collected_amount.toLocaleString()}</p></div>
            <div><p className="text-sm text-gray-600">Donors</p><p className="font-medium">{campaign.donor_count}</p></div>
            <div><p className="text-sm text-gray-600">Currency</p><p className="font-medium">{campaign.currency}</p></div>
            <div><p className="text-sm text-gray-600">Featured</p><p className="font-medium">{campaign.is_featured ? 'Yes' : 'No'}</p></div>
            <div><p className="text-sm text-gray-600">Urgent</p><p className="font-medium">{campaign.is_urgent ? 'Yes' : 'No'}</p></div>
            <div className="col-span-2"><p className="text-sm text-gray-600">Created</p><p className="font-medium">{new Date(campaign.created_at).toLocaleString()}</p></div>
          </div>
        </CardBody>
      </Card>
    </div>
  );
}
