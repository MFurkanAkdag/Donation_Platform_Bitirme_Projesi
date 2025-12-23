"use client";

import { useState, useMemo } from "react";
import { Card, CardBody, CardHeader, Input, Select, SelectItem, Chip } from "@heroui/react";
import { mockUsers } from "@/lib/mock-data/admin";

export default function AdminUsersPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [roleFilter, setRoleFilter] = useState("all");
  const [statusFilter, setStatusFilter] = useState("all");

  const filteredUsers = useMemo(() => {
    return mockUsers.filter((user) => {
      const matchesSearch = user.email.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesRole = roleFilter === "all" || user.role === roleFilter;
      const matchesStatus = statusFilter === "all" || user.status === statusFilter;
      return matchesSearch && matchesRole && matchesStatus;
    });
  }, [searchQuery, roleFilter, statusFilter]);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'active': return 'success';
      case 'suspended': return 'warning';
      case 'banned': return 'danger';
      default: return 'default';
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Users</h1>
        <p className="text-gray-600 mt-1">Manage platform users</p>
      </div>

      <Card>
        <CardBody className="p-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Input
              placeholder="Search by email..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            <Select
              selectedKeys={[roleFilter]}
              onSelectionChange={(keys) => setRoleFilter(Array.from(keys)[0] as string)}
            >
              <SelectItem key="all">All Roles</SelectItem>
              <SelectItem key="admin">Admin</SelectItem>
              <SelectItem key="donor">Donor</SelectItem>
              <SelectItem key="foundation">Foundation</SelectItem>
              <SelectItem key="beneficiary">Beneficiary</SelectItem>
            </Select>
            <Select
              selectedKeys={[statusFilter]}
              onSelectionChange={(keys) => setStatusFilter(Array.from(keys)[0] as string)}
            >
              <SelectItem key="all">All Status</SelectItem>
              <SelectItem key="active">Active</SelectItem>
              <SelectItem key="inactive">Inactive</SelectItem>
              <SelectItem key="suspended">Suspended</SelectItem>
              <SelectItem key="banned">Banned</SelectItem>
            </Select>
          </div>
        </CardBody>
      </Card>

      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold">{filteredUsers.length} Users</h2>
        </CardHeader>
        <CardBody className="p-6">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="border-b border-gray-200">
                <tr>
                  <th className="text-left py-3 px-4">Email</th>
                  <th className="text-left py-3 px-4">Role</th>
                  <th className="text-left py-3 px-4">Status</th>
                  <th className="text-left py-3 px-4">Verified</th>
                  <th className="text-left py-3 px-4">Created</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map((user) => (
                  <tr key={user.id} className="border-b border-gray-100 hover:bg-gray-50">
                    <td className="py-3 px-4 font-medium">{user.email}</td>
                    <td className="py-3 px-4">
                      <Chip size="sm" variant="flat" className="capitalize">{user.role}</Chip>
                    </td>
                    <td className="py-3 px-4">
                      <Chip size="sm" color={getStatusColor(user.status)} variant="flat">
                        {user.status}
                      </Chip>
                    </td>
                    <td className="py-3 px-4">
                      {user.email_verified ? '✓' : '✗'}
                    </td>
                    <td className="py-3 px-4 text-sm text-gray-600">
                      {new Date(user.created_at).toLocaleDateString()}
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
