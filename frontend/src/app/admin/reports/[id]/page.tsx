"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Card, CardBody, CardHeader, Chip, Button, Select, SelectItem, Textarea } from "@heroui/react";
import { useAuth } from "@/contexts/AuthContext";
import { adminDataStore } from "@/lib/adminDataStore";
import type { AdminReport, ReportPriority } from "@/types/admin";

export default function ReportDetailPage() {
  const params = useParams();
  const { user } = useAuth();
  const [report, setReport] = useState<AdminReport | null>(null);
  const [priority, setPriority] = useState<ReportPriority>('medium');
  const [resolutionNotes, setResolutionNotes] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const id = params.id as string;
    const data = adminDataStore.reports.getById(id);
    if (data) {
      setReport(data);
      setPriority(data.priority);
    }
  }, [params.id]);

  const handleAssign = async () => {
    if (!report || !user) return;
    setIsLoading(true);

    const updated = adminDataStore.reports.update(report.id, {
      assigned_to: user.id,
      status: 'in_review',
    });

    adminDataStore.auditLogs.create({
      user_id: user.id,
      action: 'report.assigned',
      entity_type: 'report',
      entity_id: report.id,
      old_values: { assigned_to: report.assigned_to },
      new_values: { assigned_to: user.id },
    });

    setReport(updated);
    setIsLoading(false);
  };

  const handleChangePriority = async () => {
    if (!report || !user || priority === report.priority) return;
    setIsLoading(true);

    const updated = adminDataStore.reports.update(report.id, { priority });

    adminDataStore.auditLogs.create({
      user_id: user.id,
      action: 'report.priority_changed',
      entity_type: 'report',
      entity_id: report.id,
      old_values: { priority: report.priority },
      new_values: { priority },
    });

    setReport(updated);
    setIsLoading(false);
  };

  const handleResolve = async () => {
    if (!report || !user || !resolutionNotes.trim()) return;
    setIsLoading(true);

    const updated = adminDataStore.reports.update(report.id, {
      status: 'resolved',
      resolved_at: new Date().toISOString(),
    });

    adminDataStore.auditLogs.create({
      user_id: user.id,
      action: 'report.resolved',
      entity_type: 'report',
      entity_id: report.id,
      old_values: { status: report.status },
      new_values: { status: 'resolved', resolution_notes: resolutionNotes },
    });

    setReport(updated);
    setIsLoading(false);
  };

  if (!report) return <div>Loading...</div>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold capitalize">{report.report_type.replace('_', ' ')}</h1>
        <Chip color={report.status === 'resolved' ? 'success' : 'warning'} variant="flat">{report.status.toUpperCase()}</Chip>
      </div>

      <Card>
        <CardBody className="p-4">
          <div className="flex flex-wrap gap-3">
            {!report.assigned_to && <Button color="primary" onClick={handleAssign} isLoading={isLoading}>Assign to Me</Button>}
            {report.status !== 'resolved' && <Button color="success" onClick={handleResolve} isLoading={isLoading} isDisabled={!resolutionNotes.trim()}>Mark Resolved</Button>}
          </div>
        </CardBody>
      </Card>

      <Card>
        <CardHeader className="pt-6 px-6"><h2 className="text-xl font-bold">Report Details</h2></CardHeader>
        <CardBody className="p-6 space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div><p className="text-sm text-gray-600">Entity Type</p><p className="font-medium capitalize">{report.entity_type}</p></div>
            <div><p className="text-sm text-gray-600">Entity ID</p><p className="font-medium">{report.entity_id}</p></div>
            <div className="col-span-2"><p className="text-sm text-gray-600">Description</p><p className="font-medium">{report.description}</p></div>
            <div><p className="text-sm text-gray-600">Created</p><p className="font-medium">{new Date(report.created_at).toLocaleString()}</p></div>
            {report.resolved_at && <div><p className="text-sm text-gray-600">Resolved</p><p className="font-medium">{new Date(report.resolved_at).toLocaleString()}</p></div>}
          </div>
          
          {report.status !== 'resolved' && (
            <>
              <div className="flex gap-3 items-end">
                <Select label="Priority" selectedKeys={[priority]} onSelectionChange={(keys) => setPriority(Array.from(keys)[0] as ReportPriority)} className="flex-1">
                  <SelectItem key="low">Low</SelectItem>
                  <SelectItem key="medium">Medium</SelectItem>
                  <SelectItem key="high">High</SelectItem>
                  <SelectItem key="critical">Critical</SelectItem>
                </Select>
                <Button onClick={handleChangePriority} isLoading={isLoading} isDisabled={priority === report.priority}>Update Priority</Button>
              </div>
              
              <Textarea label="Resolution Notes" value={resolutionNotes} onChange={(e) => setResolutionNotes(e.target.value)} minRows={3} />
            </>
          )}
        </CardBody>
      </Card>
    </div>
  );
}
