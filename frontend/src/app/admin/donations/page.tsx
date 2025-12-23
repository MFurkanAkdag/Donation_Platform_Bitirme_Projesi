"use client";
import { Card, CardBody, CardHeader, Chip } from "@heroui/react";
import { mockDonations } from "@/lib/mock-data/admin";

export default function AdminDonationsPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Donations</h1>
      <Card>
        <CardHeader className="pt-6 px-6"><h2 className="text-xl font-bold">{mockDonations.length} Donations</h2></CardHeader>
        <CardBody className="p-6">
          <table className="w-full">
            <thead className="border-b"><tr><th className="text-left py-3 px-4">Amount</th><th className="text-left py-3 px-4">Status</th><th className="text-left py-3 px-4">Anonymous</th><th className="text-left py-3 px-4">Date</th></tr></thead>
            <tbody>
              {mockDonations.map((d) => (
                <tr key={d.id} className="border-b hover:bg-gray-50">
                  <td className="py-3 px-4 font-medium">₺{d.amount}</td>
                  <td className="py-3 px-4"><Chip size="sm" color={d.status === 'completed' ? 'success' : 'warning'} variant="flat">{d.status}</Chip></td>
                  <td className="py-3 px-4">{d.is_anonymous ? 'Yes' : 'No'}</td>
                  <td className="py-3 px-4 text-sm">{new Date(d.created_at).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </CardBody>
      </Card>
    </div>
  );
}
