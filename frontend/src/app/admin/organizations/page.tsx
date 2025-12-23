"use client";

import { useRouter } from "next/navigation";
import { useState, useMemo } from "react";
import { Card, CardBody, CardHeader, Input, Select, SelectItem, Chip } from "@heroui/react";
import { mockOrganizations } from "@/lib/mock-data/admin";
import type { AdminOrganization } from "@/types/admin";

export default function AdminOrganizationsPage() {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [typeFilter, setTypeFilter] = useState("all");

  const filteredOrganizations = useMemo(() => {
    return mockOrganizations.filter((org) => {
      const matchesSearch = org.legal_name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        org.trade_name?.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesStatus = statusFilter === "all" || org.verification_status === statusFilter;
      const matchesType = typeFilter === "all" || org.organization_type === typeFilter;
      return matchesSearch && matchesStatus && matchesType;
    });
  }, [searchQuery, statusFilter, typeFilter]);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'approved': return 'success';
      case 'in_review': return 'warning';
      case 'rejected': return 'danger';
      default: return 'default';
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Organizations</h1>
        <p className="text-gray-600 mt-1">Manage organization verifications and settings</p>
      </div>

      {/* Filters */}
      <Card>
        <CardBody className="p-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Input
              placeholder="Search by name..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            <Select
              selectedKeys={[statusFilter]}
              onSelectionChange={(keys) => setStatusFilter(Array.from(keys)[0] as string)}
              placeholder="Filter by status"
            >
              <SelectItem key="all">All Status</SelectItem>
              <SelectItem key="pending">Pending</SelectItem>
              <SelectItem key="in_review">In Review</SelectItem>
              <SelectItem key="approved">Approved</SelectItem>
              <SelectItem key="rejected">Rejected</SelectItem>
            </Select>
            <Select
              selectedKeys={[typeFilter]}
              onSelectionChange={(keys) => setTypeFilter(Array.from(keys)[0] as string)}
              placeholder="Filter by type"
            >
              <SelectItem key="all">All Types</SelectItem>
              <SelectItem key="foundation">Foundation</SelectItem>
              <SelectItem key="association">Association</SelectItem>
              <SelectItem key="ngo">NGO</SelectItem>
            </Select>
          </div>
        </CardBody>
      </Card>

      {/* Organizations Table */}
      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold text-gray-900">
            {filteredOrganizations.length} Organization{filteredOrganizations.length !== 1 ? 's' : ''}
          </h2>
        </CardHeader>
        <CardBody className="p-6">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="border-b border-gray-200">
                <tr>
                  <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Legal Name</th>
                  <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Type</th>
                  <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Status</th>
                  <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Featured</th>
                  <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Created</th>
                </tr>
              </thead>
              <tbody>
                {filteredOrganizations.map((org) => (
                  <tr 
                    key={org.id} 
                    className="border-b border-gray-100 hover:bg-gray-50 cursor-pointer"
                    onClick={() => router.push(`/admin/organizations/${org.id}`)}
                  >
                    <td className="py-3 px-4">
                      <div>
                        <p className="font-medium text-gray-900">{org.legal_name}</p>
                        {org.trade_name && (
                          <p className="text-sm text-gray-600">{org.trade_name}</p>
                        )}
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <span className="text-sm text-gray-700 capitalize">{org.organization_type}</span>
                    </td>
                    <td className="py-3 px-4">
                      <Chip size="sm" color={getStatusColor(org.verification_status)} variant="flat">
                        {org.verification_status.replace('_', ' ')}
                      </Chip>
                    </td>
                    <td className="py-3 px-4">
                      {org.is_featured ? (
                        <Chip size="sm" color="primary" variant="flat">Featured</Chip>
                      ) : (
                        <span className="text-sm text-gray-500">-</span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-sm text-gray-600">
                      {new Date(org.created_at).toLocaleDateString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </CardBody>
      </Card>
    </div>
  );
}
