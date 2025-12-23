"use client";
import { Card, CardBody, CardHeader } from "@heroui/react";
import { mockAuditLogs } from "@/lib/mock-data/admin";

export default function AdminAuditLogsPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Audit Logs</h1>
      <Card>
        <CardHeader className="pt-6 px-6"><h2 className="text-xl font-bold">{mockAuditLogs.length} Logs</h2></CardHeader>
        <CardBody className="p-6">
          <table className="w-full">
            <thead className="border-b"><tr><th className="text-left py-3 px-4">Action</th><th className="text-left py-3 px-4">Entity</th><th className="text-left py-3 px-4">User ID</th><th className="text-left py-3 px-4">Date</th></tr></thead>
            <tbody>
              {mockAuditLogs.map((log) => (
                <tr key={log.id} className="border-b hover:bg-gray-50">
                  <td className="py-3 px-4 font-medium">{log.action}</td>
                  <td className="py-3 px-4">{log.entity_type}</td>
                  <td className="py-3 px-4 text-sm">{log.user_id}</td>
                  <td className="py-3 px-4 text-sm">{new Date(log.created_at).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </CardBody>
      </Card>
    </div>
  );
}
