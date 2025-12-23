"use client";

import { Card, CardBody, CardHeader } from "@heroui/react";
import { mockDashboardStats, mockAuditLogs } from "@/lib/mock-data/admin";

export default function AdminDashboardPage() {
  const stats = mockDashboardStats;
  const recentLogs = mockAuditLogs.slice(0, 5);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Admin Dashboard</h1>
        <p className="text-gray-600 mt-1">Platform overview and key metrics</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardBody className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total Users</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{stats.total_users}</p>
              </div>
              <div className="text-4xl">👥</div>
            </div>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Pending Org Verifications</p>
                <p className="text-3xl font-bold text-orange-600 mt-2">{stats.pending_org_verifications}</p>
              </div>
              <div className="text-4xl">🏢</div>
            </div>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Campaign Approvals</p>
                <p className="text-3xl font-bold text-orange-600 mt-2">{stats.pending_campaign_approvals}</p>
              </div>
              <div className="text-4xl">🎯</div>
            </div>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Evidence Reviews</p>
                <p className="text-3xl font-bold text-orange-600 mt-2">{stats.pending_evidence_reviews}</p>
              </div>
              <div className="text-4xl">📋</div>
            </div>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Pending Reports</p>
                <p className="text-3xl font-bold text-red-600 mt-2">{stats.pending_reports}</p>
              </div>
              <div className="text-4xl">⚠️</div>
            </div>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Donations Today</p>
                <p className="text-3xl font-bold text-green-600 mt-2">{stats.total_donations_today}</p>
              </div>
              <div className="text-4xl">💰</div>
            </div>
          </CardBody>
        </Card>

        <Card className="md:col-span-2">
          <CardBody className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Amount Today</p>
                <p className="text-3xl font-bold text-green-600 mt-2">
                  ₺{stats.total_amount_today.toLocaleString()}
                </p>
              </div>
              <div className="text-4xl">💵</div>
            </div>
          </CardBody>
        </Card>
      </div>

      {/* Recent Activity */}
      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">Recent Activity</h2>
        </CardHeader>
        <CardBody className="p-6">
          <div className="space-y-4">
            {recentLogs.map((log) => (
              <div key={log.id} className="flex items-start gap-4 border-b border-gray-100 pb-4 last:border-0">
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-gray-900">{log.action}</span>
                    <span className="text-sm text-gray-500">•</span>
                    <span className="text-sm text-gray-600">{log.entity_type}</span>
                  </div>
                  <p className="text-sm text-gray-600 mt-1">
                    Entity ID: {log.entity_id}
                  </p>
                  <p className="text-xs text-gray-500 mt-1">
                    {new Date(log.created_at).toLocaleString()}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </CardBody>
      </Card>
    </div>
  );
}
