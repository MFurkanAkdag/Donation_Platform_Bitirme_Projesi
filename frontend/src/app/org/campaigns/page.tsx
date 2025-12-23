"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Card, CardBody, CardHeader, Chip, Button, Input } from "@heroui/react";
import { getCampaigns } from "@/lib/mockOrgData";
import type { Campaign } from "@/types/organization";

export default function OrgCampaignsPage() {
  const router = useRouter();
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('all');

  useEffect(() => {
    setCampaigns(getCampaigns());
  }, []);

  const filteredCampaigns = campaigns.filter(c => {
    const matchesSearch = c.title.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = statusFilter === 'all' || c.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'active': return 'success';
      case 'completed': return 'default';
      case 'paused': return 'warning';
      case 'cancelled': return 'danger';
      case 'draft': return 'default';
      default: return 'primary';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">My Campaigns</h1>
        <Button
          color="primary"
          onClick={() => router.push('/org/campaigns/new')}
        >
          + Create Campaign
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardBody className="p-4">
          <div className="flex flex-col md:flex-row gap-4">
            <Input
              placeholder="Search campaigns..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="flex-1"
            />
            <div className="flex gap-2">
              {['all', 'active', 'completed', 'draft', 'paused'].map(status => (
                <Button
                  key={status}
                  size="sm"
                  variant={statusFilter === status ? 'solid' : 'bordered'}
                  color={statusFilter === status ? 'primary' : 'default'}
                  onClick={() => setStatusFilter(status)}
                >
                  {status === 'all' ? 'All' : status.charAt(0).toUpperCase() + status.slice(1)}
                </Button>
              ))}
            </div>
          </div>
        </CardBody>
      </Card>

      {/* Campaigns List */}
      {filteredCampaigns.length === 0 ? (
        <Card>
          <CardBody className="p-12 text-center">
            <div className="text-gray-500">
              <p className="text-lg mb-2">No campaigns found</p>
              <p className="text-sm">
                {searchQuery || statusFilter !== 'all'
                  ? 'Try adjusting your filters'
                  : 'Create your first campaign to get started'}
              </p>
              {!searchQuery && statusFilter === 'all' && (
                <Button
                  color="primary"
                  className="mt-4"
                  onClick={() => router.push('/org/campaigns/new')}
                >
                  Create Campaign
                </Button>
              )}
            </div>
          </CardBody>
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-4">
          {filteredCampaigns.map((campaign) => (
            <Card key={campaign.id}>
              <CardBody className="p-6">
                <div className="flex items-start gap-4">
                  {campaign.image_url && (
                    <img
                      src={campaign.image_url}
                      alt={campaign.title}
                      className="w-32 h-32 object-cover rounded-lg"
                    />
                  )}
                  <div className="flex-1">
                    <div className="flex items-start justify-between mb-2">
                      <div>
                        <h3 className="text-xl font-bold text-gray-900">{campaign.title}</h3>
                        <p className="text-gray-600 mt-1">{campaign.description}</p>
                      </div>
                      <Chip color={getStatusColor(campaign.status)} variant="flat">
                        {campaign.status}
                      </Chip>
                    </div>

                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-4">
                      <div>
                        <p className="text-sm text-gray-600">Goal</p>
                        <p className="font-semibold">₺{campaign.goal_amount.toLocaleString()}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Collected</p>
                        <p className="font-semibold">₺{campaign.collected_amount.toLocaleString()}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Progress</p>
                        <p className="font-semibold">
                          {Math.round((campaign.collected_amount / campaign.goal_amount) * 100)}%
                        </p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">End Date</p>
                        <p className="font-semibold">
                          {new Date(campaign.end_date).toLocaleDateString()}
                        </p>
                      </div>
                    </div>

                    <div className="flex gap-2 mt-4">
                      <Button
                        size="sm"
                        variant="flat"
                        onClick={() => router.push(`/org/campaigns/${campaign.id}/edit`)}
                      >
                        Edit
                      </Button>
                      <Link href={`/campaigns/${campaign.id}`}>
                        <Button size="sm" variant="bordered">
                          View Public Page
                        </Button>
                      </Link>
                    </div>
                  </div>
                </div>
              </CardBody>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
