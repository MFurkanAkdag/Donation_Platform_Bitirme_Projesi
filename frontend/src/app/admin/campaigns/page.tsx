"use client";

import { useState, useMemo } from "react";
import { Card, CardBody, CardHeader, Input, Select, SelectItem, Chip } from "@heroui/react";
import { mockCampaigns } from "@/lib/mock-data/admin";

export default function AdminCampaignsPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");

  const filteredCampaigns = useMemo(() => {
    return mockCampaigns.filter((campaign) => {
      const matchesSearch = campaign.title.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesStatus = statusFilter === "all" || campaign.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [searchQuery, statusFilter]);

  const getStatusColor = (status: string) => {
    const colors: Record<string, any> = {
      active: 'success', completed: 'default', pending_approval: 'warning',
      draft: 'default', paused: 'warning', cancelled: 'danger'
    };
    return colors[status] || 'default';
  };

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Campaigns</h1>
      <Card>
        <CardBody className="p-4">
          <div className="grid grid-cols-2 gap-4">
            <Input placeholder="Search..." value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} />
            <Select selectedKeys={[statusFilter]} onSelectionChange={(keys) => setStatusFilter(Array.from(keys)[0] as string)}>
              <SelectItem key="all">All Status</SelectItem>
              <SelectItem key="active">Active</SelectItem>
              <SelectItem key="pending_approval">Pending Approval</SelectItem>
              <SelectItem key="completed">Completed</SelectItem>
            </Select>
          </div>
        </CardBody>
      </Card>
      <Card>
        <CardHeader className="pt-6 px-6"><h2 className="text-xl font-bold">{filteredCampaigns.length} Campaigns</h2></CardHeader>
        <CardBody className="p-6">
          <table className="w-full">
            <thead className="border-b">
              <tr><th className="text-left py-3 px-4">Title</th><th className="text-left py-3 px-4">Status</th><th className="text-left py-3 px-4">Goal</th><th className="text-left py-3 px-4">Collected</th></tr>
            </thead>
            <tbody>
              {filteredCampaigns.map((c) => (
                <tr key={c.id} className="border-b hover:bg-gray-50">
                  <td className="py-3 px-4 font-medium">{c.title}</td>
                  <td className="py-3 px-4"><Chip size="sm" color={getStatusColor(c.status)} variant="flat">{c.status.replace('_', ' ')}</Chip></td>
                  <td className="py-3 px-4">₺{c.goal_amount.toLocaleString()}</td>
                  <td className="py-3 px-4">₺{c.collected_amount.toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </CardBody>
      </Card>
    </div>
  );
}
