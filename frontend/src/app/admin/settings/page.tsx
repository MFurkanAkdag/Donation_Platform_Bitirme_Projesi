"use client";
import { Card, CardBody, CardHeader, Chip } from "@heroui/react";
import { mockSystemSettings } from "@/lib/mock-data/admin";

export default function AdminSettingsPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">System Settings</h1>
      <Card>
        <CardHeader className="pt-6 px-6"><h2 className="text-xl font-bold">{mockSystemSettings.length} Settings</h2></CardHeader>
        <CardBody className="p-6">
          <table className="w-full">
            <thead className="border-b"><tr><th className="text-left py-3 px-4">Key</th><th className="text-left py-3 px-4">Value</th><th className="text-left py-3 px-4">Type</th><th className="text-left py-3 px-4">Public</th></tr></thead>
            <tbody>
              {mockSystemSettings.map((s) => (
                <tr key={s.id} className="border-b hover:bg-gray-50">
                  <td className="py-3 px-4 font-medium">{s.setting_key}</td>
                  <td className="py-3 px-4">{s.setting_value}</td>
                  <td className="py-3 px-4"><Chip size="sm" variant="flat">{s.value_type}</Chip></td>
                  <td className="py-3 px-4">{s.is_public ? 'Yes' : 'No'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </CardBody>
      </Card>
    </div>
  );
}
