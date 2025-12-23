"use client";

import { useEffect, useState } from "react";
import { Card, CardBody, CardHeader, Chip, Progress, Divider } from "@heroui/react";
import Link from "next/link";
import { getOrganization, getTransparencyScore, getCampaigns } from "@/lib/mockOrgData";
import type { Organization, TransparencyScore, Campaign } from "@/types/organization";

export default function OrgDashboardPage() {
  const [org, setOrg] = useState<Organization | null>(null);
  const [transparency, setTransparency] = useState<TransparencyScore | null>(null);
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);

  useEffect(() => {
    setOrg(getOrganization());
    setTransparency(getTransparencyScore());
    setCampaigns(getCampaigns());
  }, []);

  if (!org || !transparency) {
    return <div>Loading...</div>;
  }

  const activeCampaigns = campaigns.filter(c => c.status === 'active');
  const totalCollected = campaigns.reduce((sum, c) => sum + c.collected_amount, 0);

  const getVerificationColor = (status: string) => {
    switch (status) {
      case 'approved': return 'success';
      case 'in_review': return 'warning';
      case 'rejected': return 'danger';
      default: return 'default';
    }
  };

  const getScoreColor = (score: number) => {
    if (score >= 90) return 'success';
    if (score >= 75) return 'warning';
    return 'default';
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            {org.trade_name || org.legal_name}
          </h1>
          <p className="text-gray-600 mt-1">{org.organization_type.toUpperCase()}</p>
        </div>
        <Chip color={getVerificationColor(org.verification_status)} variant="flat" size="lg">
          {org.verification_status.replace('_', ' ').toUpperCase()}
        </Chip>
      </div>

      {/* Key Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardBody className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Transparency Score</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">
                  {transparency.current_score}
                </p>
              </div>
              <div className="text-4xl">🏆</div>
            </div>
            <Progress
              value={transparency.current_score}
              color={getScoreColor(transparency.current_score)}
              className="mt-4"
            />
          </CardBody>
        </Card>

        <Card>
          <CardBody className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Active Campaigns</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">
                  {transparency.active_campaigns}
                </p>
              </div>
              <div className="text-4xl">🎯</div>
            </div>
            <p className="text-sm text-gray-500 mt-4">
              {transparency.total_campaigns} total campaigns
            </p>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total Collected</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">
                  ₺{(totalCollected / 1000).toFixed(0)}K
                </p>
              </div>
              <div className="text-4xl">💰</div>
            </div>
            <p className="text-sm text-gray-500 mt-4">
              Across all campaigns
            </p>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Completed</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">
                  {transparency.completed_campaigns}
                </p>
              </div>
              <div className="text-4xl">✅</div>
            </div>
            <p className="text-sm text-gray-500 mt-4">
              Successfully finished
            </p>
          </CardBody>
        </Card>
      </div>

      {/* Transparency Details */}
      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">Transparency Metrics</h2>
        </CardHeader>
        <CardBody className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <div className="flex justify-between items-center mb-2">
                <span className="text-sm text-gray-600">Reports</span>
                <span className="text-sm font-medium">
                  {transparency.verified_reports}/{transparency.total_reports} verified
                </span>
              </div>
              <Progress
                value={(transparency.verified_reports / transparency.total_reports) * 100}
                color="success"
              />
            </div>
            <div>
              <div className="flex justify-between items-center mb-2">
                <span className="text-sm text-gray-600">Evidence</span>
                <span className="text-sm font-medium">
                  {transparency.verified_evidence}/{transparency.total_evidence} verified
                </span>
              </div>
              <Progress
                value={(transparency.verified_evidence / transparency.total_evidence) * 100}
                color="success"
              />
            </div>
          </div>
          <Divider className="my-4" />
          <div className="flex items-center justify-between text-sm">
            <span className="text-gray-600">Average Report Time:</span>
            <span className="font-medium">{transparency.avg_report_time_days} days</span>
          </div>
        </CardBody>
      </Card>

      {/* Recent Campaigns */}
      <Card>
        <CardHeader className="pb-0 pt-6 px-6 flex justify-between items-center">
          <h2 className="text-xl font-bold text-gray-900">Recent Campaigns</h2>
          <Link
            href="/org/campaigns"
            className="text-blue-600 hover:text-blue-700 text-sm font-medium"
          >
            View All →
          </Link>
        </CardHeader>
        <CardBody className="p-6">
          {activeCampaigns.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              <p className="mb-2">No active campaigns</p>
              <Link
                href="/org/campaigns/new"
                className="text-blue-600 hover:text-blue-700 font-medium"
              >
                Create your first campaign
              </Link>
            </div>
          ) : (
            <div className="space-y-4">
              {activeCampaigns.slice(0, 3).map((campaign) => (
                <div key={campaign.id} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex items-start justify-between mb-2">
                    <div className="flex-1">
                      <h3 className="font-semibold text-gray-900">{campaign.title}</h3>
                      <p className="text-sm text-gray-600 mt-1">{campaign.description}</p>
                    </div>
                    <Chip size="sm" color="primary" variant="flat">
                      {campaign.status}
                    </Chip>
                  </div>
                  <div className="mt-3">
                    <div className="flex justify-between text-sm mb-1">
                      <span className="text-gray-600">Progress</span>
                      <span className="font-medium">
                        ₺{campaign.collected_amount.toLocaleString()} / ₺{campaign.goal_amount.toLocaleString()}
                      </span>
                    </div>
                    <Progress
                      value={(campaign.collected_amount / campaign.goal_amount) * 100}
                      color="primary"
                    />
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardBody>
      </Card>
    </div>
  );
}
