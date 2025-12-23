"use client";
import { Card, CardBody, CardHeader, Chip } from "@heroui/react";
import { mockEvidences } from "@/lib/mock-data/admin";

export default function AdminEvidencesPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Evidences</h1>
      <Card>
        <CardHeader className="pt-6 px-6"><h2 className="text-xl font-bold">{mockEvidences.length} Evidences</h2></CardHeader>
        <CardBody className="p-6">
          <table className="w-full">
            <thead className="border-b"><tr><th className="text-left py-3 px-4">Title</th><th className="text-left py-3 px-4">Type</th><th className="text-left py-3 px-4">Status</th><th className="text-left py-3 px-4">Uploaded</th></tr></thead>
            <tbody>
              {mockEvidences.map((e) => (
                <tr key={e.id} className="border-b hover:bg-gray-50">
                  <td className="py-3 px-4 font-medium">{e.title}</td>
                  <td className="py-3 px-4 text-sm capitalize">{e.evidence_type}</td>
                  <td className="py-3 px-4"><Chip size="sm" color={e.status === 'approved' ? 'success' : 'warning'} variant="flat">{e.status}</Chip></td>
                  <td className="py-3 px-4 text-sm">{new Date(e.uploaded_at).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </CardBody>
      </Card>
    </div>
  );
}
