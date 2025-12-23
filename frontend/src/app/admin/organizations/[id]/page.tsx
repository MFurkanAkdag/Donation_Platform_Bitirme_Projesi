"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { Card, CardBody, CardHeader, Chip, Button, Modal, ModalContent, ModalHeader, ModalBody, ModalFooter, Textarea } from "@heroui/react";
import { useAuth } from "@/contexts/AuthContext";
import { adminDataStore } from "@/lib/adminDataStore";
import type { AdminOrganization } from "@/types/admin";

export default function OrganizationDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { user } = useAuth();
  const [org, setOrg] = useState<AdminOrganization | null>(null);
  const [isRejectModalOpen, setIsRejectModalOpen] = useState(false);
  const [rejectionReason, setRejectionReason] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const id = params.id as string;
    const data = adminDataStore.organizations.getById(id);
    setOrg(data);
  }, [params.id]);

  const handleApprove = async () => {
    if (!org || !user) return;
    setIsLoading(true);

    const updated = adminDataStore.organizations.update(org.id, {
      verification_status: 'approved',
      verified_at: new Date().toISOString(),
      rejection_reason: undefined,
    });

    adminDataStore.auditLogs.create({
      user_id: user.id,
      action: 'organization.approved',
      entity_type: 'organization',
      entity_id: org.id,
      old_values: { verification_status: org.verification_status },
      new_values: { verification_status: 'approved' },
    });

    setOrg(updated);
    setIsLoading(false);
  };

  const handleReject = async () => {
    if (!org || !user || !rejectionReason.trim()) return;
    setIsLoading(true);

    const updated = adminDataStore.organizations.update(org.id, {
      verification_status: 'rejected',
      rejection_reason: rejectionReason,
    });

    adminDataStore.auditLogs.create({
      user_id: user.id,
      action: 'organization.rejected',
      entity_type: 'organization',
      entity_id: org.id,
      old_values: { verification_status: org.verification_status },
      new_values: { verification_status: 'rejected', rejection_reason: rejectionReason },
    });

    setOrg(updated);
    setIsRejectModalOpen(false);
    setRejectionReason("");
    setIsLoading(false);
  };

  if (!org) {
    return <div>Loading...</div>;
  }

  const getStatusColor = (status: string) => {
    const colors: Record<string, any> = {
      approved: 'success',
      in_review: 'warning',
      rejected: 'danger',
      pending: 'default',
    };
    return colors[status] || 'default';
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">{org.legal_name}</h1>
          <p className="text-gray-600 mt-1">{org.trade_name}</p>
        </div>
        <Chip color={getStatusColor(org.verification_status)} variant="flat" size="lg">
          {org.verification_status.replace('_', ' ').toUpperCase()}
        </Chip>
      </div>

      {/* Actions */}
      {org.verification_status !== 'approved' && (
        <Card>
          <CardBody className="p-4">
            <div className="flex gap-3">
              <Button color="success" onClick={handleApprove} isLoading={isLoading}>
                Approve Organization
              </Button>
              <Button color="danger" variant="bordered" onClick={() => setIsRejectModalOpen(true)}>
                Reject Organization
              </Button>
            </div>
          </CardBody>
        </Card>
      )}

      {/* Organization Details */}
      <Card>
        <CardHeader className="pb-0 pt-6 px-6">
          <h2 className="text-xl font-bold">Organization Details</h2>
        </CardHeader>
        <CardBody className="p-6">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-gray-600">Legal Name</p>
              <p className="font-medium">{org.legal_name}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Trade Name</p>
              <p className="font-medium">{org.trade_name || '-'}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Type</p>
              <p className="font-medium capitalize">{org.organization_type}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Featured</p>
              <p className="font-medium">{org.is_featured ? 'Yes' : 'No'}</p>
            </div>
            <div className="col-span-2">
              <p className="text-sm text-gray-600">Verified At</p>
              <p className="font-medium">{org.verified_at ? new Date(org.verified_at).toLocaleString() : '-'}</p>
            </div>
            {org.rejection_reason && (
              <div className="col-span-2">
                <p className="text-sm text-gray-600">Rejection Reason</p>
                <p className="font-medium text-red-600">{org.rejection_reason}</p>
              </div>
            )}
            <div className="col-span-2">
              <p className="text-sm text-gray-600">Created At</p>
              <p className="font-medium">{new Date(org.created_at).toLocaleString()}</p>
            </div>
          </div>
        </CardBody>
      </Card>

      {/* Reject Modal */}
      <Modal isOpen={isRejectModalOpen} onClose={() => setIsRejectModalOpen(false)}>
        <ModalContent>
          <ModalHeader>Reject Organization</ModalHeader>
          <ModalBody>
            <Textarea
              label="Rejection Reason"
              placeholder="Explain why this organization is being rejected..."
              value={rejectionReason}
              onChange={(e) => setRejectionReason(e.target.value)}
              minRows={4}
              isRequired
            />
          </ModalBody>
          <ModalFooter>
            <Button variant="light" onClick={() => setIsRejectModalOpen(false)}>
              Cancel
            </Button>
            <Button
              color="danger"
              onClick={handleReject}
              isLoading={isLoading}
              isDisabled={!rejectionReason.trim()}
            >
              Reject
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>
    </div>
  );
}
