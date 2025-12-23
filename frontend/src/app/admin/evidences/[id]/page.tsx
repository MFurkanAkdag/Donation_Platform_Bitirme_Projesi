"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Card, CardBody, CardHeader, Chip, Button, Modal, ModalContent, ModalHeader, ModalBody, ModalFooter, Textarea } from "@heroui/react";
import { useAuth } from "@/contexts/AuthContext";
import { adminDataStore } from "@/lib/adminDataStore";
import type { AdminEvidence } from "@/types/admin";

export default function EvidenceDetailPage() {
  const params = useParams();
  const { user } = useAuth();
  const [evidence, setEvidence] = useState<AdminEvidence | null>(null);
  const [isRejectModalOpen, setIsRejectModalOpen] = useState(false);
  const [rejectionReason, setRejectionReason] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const id = params.id as string;
    const data = adminDataStore.evidences.getById(id);
    setEvidence(data);
  }, [params.id]);

  const handleApprove = async () => {
    if (!evidence || !user) return;
    setIsLoading(true);

    const updated = adminDataStore.evidences.update(evidence.id, {
      status: 'approved',
      reviewed_at: new Date().toISOString(),
      reviewer_id: user.id,
    });

    adminDataStore.auditLogs.create({
      user_id: user.id,
      action: 'evidence.approved',
      entity_type: 'evidence',
      entity_id: evidence.id,
      old_values: { status: evidence.status },
      new_values: { status: 'approved' },
    });

    setEvidence(updated);
    setIsLoading(false);
  };

  const handleReject = async () => {
    if (!evidence || !user || !rejectionReason.trim()) return;
    setIsLoading(true);

    const updated = adminDataStore.evidences.update(evidence.id, {
      status: 'rejected',
      reviewed_at: new Date().toISOString(),
      reviewer_id: user.id,
    });

    adminDataStore.auditLogs.create({
      user_id: user.id,
      action: 'evidence.rejected',
      entity_type: 'evidence',
      entity_id: evidence.id,
      old_values: { status: evidence.status },
      new_values: { status: 'rejected' },
    });

    setEvidence(updated);
    setIsRejectModalOpen(false);
    setRejectionReason("");
    setIsLoading(false);
  };

  if (!evidence) return <div>Loading...</div>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">{evidence.title}</h1>
        <Chip color={evidence.status === 'approved' ? 'success' : evidence.status === 'rejected' ? 'danger' : 'warning'} variant="flat">
          {evidence.status.toUpperCase()}
        </Chip>
      </div>

      {evidence.status === 'pending' && (
        <Card>
          <CardBody className="p-4">
            <div className="flex gap-3">
              <Button color="success" onClick={handleApprove} isLoading={isLoading}>Approve Evidence</Button>
              <Button color="danger" variant="bordered" onClick={() => setIsRejectModalOpen(true)}>Reject Evidence</Button>
            </div>
          </CardBody>
        </Card>
      )}

      <Card>
        <CardHeader className="pt-6 px-6"><h2 className="text-xl font-bold">Evidence Details</h2></CardHeader>
        <CardBody className="p-6">
          <div className="grid grid-cols-2 gap-4">
            <div><p className="text-sm text-gray-600">Type</p><p className="font-medium capitalize">{evidence.evidence_type}</p></div>
            <div><p className="text-sm text-gray-600">Amount Spent</p><p className="font-medium">₺{evidence.amount_spent?.toLocaleString() || '-'}</p></div>
            <div><p className="text-sm text-gray-600">Vendor</p><p className="font-medium">{evidence.vendor_name || '-'}</p></div>
            <div><p className="text-sm text-gray-600">Uploaded</p><p className="font-medium">{new Date(evidence.uploaded_at).toLocaleDateString()}</p></div>
            {evidence.reviewed_at && <div className="col-span-2"><p className="text-sm text-gray-600">Reviewed At</p><p className="font-medium">{new Date(evidence.reviewed_at).toLocaleString()}</p></div>}
          </div>
        </CardBody>
      </Card>

      <Modal isOpen={isRejectModalOpen} onClose={() => setIsRejectModalOpen(false)}>
        <ModalContent>
          <ModalHeader>Reject Evidence</ModalHeader>
          <ModalBody>
            <Textarea label="Rejection Reason" value={rejectionReason} onChange={(e) => setRejectionReason(e.target.value)} minRows={4} isRequired />
          </ModalBody>
          <ModalFooter>
            <Button variant="light" onClick={() => setIsRejectModalOpen(false)}>Cancel</Button>
            <Button color="danger" onClick={handleReject} isLoading={isLoading} isDisabled={!rejectionReason.trim()}>Reject</Button>
          </ModalFooter>
        </ModalContent>
      </Modal>
    </div>
  );
}
