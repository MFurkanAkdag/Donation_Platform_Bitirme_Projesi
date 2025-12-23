"use client";
import { Card, CardBody, CardHeader, Chip } from "@heroui/react";
import { mockReports } from "@/lib/mock-data/admin";

export default function AdminReportsPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Reports</h1>
      <Card>
        <CardHeader className="pt-6 px-6"><h2 className="text-xl font-bold">{mockReports.length} Reports</h2></CardHeader>
        <CardBody className="p-6">
          <table className="w-full">
            <thead className="border-b"><tr><th className="text-left py-3 px-4">Type</th><th className="text-left py-3 px-4">Priority</th><th className="text-left py-3 px-4">Status</th><th className="text-left py-3 px-4">Description</th></tr></thead>
            <tbody>
              {mockReports.map((r) => (
                <tr key={r.id} className="border-b hover:bg-gray-50">
                  <td className="py-3 px-4 text-sm capitalize">{r.report_type.replace('_', ' ')}</td>
                  <td className="py-3 px-4"><Chip size="sm" color={r.priority === 'high' ? 'danger' : 'default'} variant="flat">{r.priority}</Chip></td>
                  <td className="py-3 px-4"><Chip size="sm" color={r.status === 'resolved' ? 'success' : 'warning'} variant="flat">{r.status}</Chip></td>
                  <td className="py-3 px-4 text-sm">{r.description.substring(0, 50)}...</td>
                </tr>
              ))}
            </tbody>
          </table>
        </CardBody>
      </Card>
    </div>
  );
}
