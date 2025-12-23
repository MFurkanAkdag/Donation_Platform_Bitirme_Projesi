"use client";

import { useEffect, useState } from "react";
import { Card, CardBody, CardHeader, Input, Select, SelectItem, Button, Chip } from "@heroui/react";
import { getDocuments, addDocument, deleteDocument } from "@/lib/mockOrgData";
import type { OrganizationDocument } from "@/types/organization";

export default function OrgDocumentsPage() {
  const [documents, setDocuments] = useState<OrganizationDocument[]>([]);
  const [isAdding, setIsAdding] = useState(false);
  const [formData, setFormData] = useState<Partial<OrganizationDocument>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    setDocuments(getDocuments());
  }, []);

  const resetForm = () => {
    setFormData({});
    setErrors({});
    setIsAdding(false);
  };

  const handleChange = (field: keyof OrganizationDocument, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    setErrors(prev => ({ ...prev, [field]: '' }));
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.document_type) {
      newErrors.document_type = 'Document type is required';
    }

    if (!formData.document_name?.trim()) {
      newErrors.document_name = 'Document name is required';
    }

    if (!formData.file_url?.trim()) {
      newErrors.file_url = 'File URL is required';
    } else if (!/^https?:\/\/.+/.test(formData.file_url)) {
      newErrors.file_url = 'Must be a valid URL';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = () => {
    if (!validate()) return;

    addDocument({
      organization_id: 'org_1',
      document_type: formData.document_type!,
      document_name: formData.document_name!,
      file_url: formData.file_url!,
      is_verified: false,
      uploaded_at: new Date().toISOString(),
      expires_at: formData.expires_at,
    });
    setDocuments(getDocuments());
    resetForm();
  };

  const handleDelete = (id: string) => {
    if (confirm('Are you sure you want to delete this document?')) {
      deleteDocument(id);
      setDocuments(getDocuments());
    }
  };

  const isExpired = (expiresAt?: string) => {
    if (!expiresAt) return false;
    return new Date(expiresAt) < new Date();
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">Documents</h1>
        <Button color="primary" onClick={() => setIsAdding(true)} isDisabled={isAdding}>
          + Upload Document
        </Button>
      </div>

      {/* Add Form */}
      {isAdding && (
        <Card>
          <CardHeader className="pb-0 pt-6 px-6">
            <h2 className="text-xl font-bold">Upload New Document</h2>
          </CardHeader>
          <CardBody className="p-6 space-y-4">
            <Select
              label="Document Type"
              selectedKeys={formData.document_type ? [formData.document_type] : []}
              onSelectionChange={(keys) => handleChange('document_type', Array.from(keys)[0])}
              isRequired
              isInvalid={!!errors.document_type}
              errorMessage={errors.document_type}
            >
              <SelectItem key="registration_certificate">Registration Certificate</SelectItem>
              <SelectItem key="tax_certificate">Tax Certificate</SelectItem>
              <SelectItem key="bank_letter">Bank Letter</SelectItem>
              <SelectItem key="activity_report">Activity Report</SelectItem>
              <SelectItem key="financial_statement">Financial Statement</SelectItem>
              <SelectItem key="other">Other</SelectItem>
            </Select>

            <Input
              label="Document Name"
              value={formData.document_name || ''}
              onChange={(e) => handleChange('document_name', e.target.value)}
              placeholder="e.g., 2024 Registration Certificate"
              isRequired
              isInvalid={!!errors.document_name}
              errorMessage={errors.document_name}
            />

            <Input
              label="File URL"
              value={formData.file_url || ''}
              onChange={(e) => handleChange('file_url', e.target.value)}
              placeholder="https://example.com/document.pdf"
              isRequired
              isInvalid={!!errors.file_url}
              errorMessage={errors.file_url}
              description="Upload to cloud storage and paste URL"
            />

            <Input
              label="Expiration Date (Optional)"
              type="date"
              value={formData.expires_at || ''}
              onChange={(e) => handleChange('expires_at', e.target.value)}
            />

            <div className="flex gap-2 justify-end">
              <Button variant="bordered" onClick={resetForm}>Cancel</Button>
              <Button color="primary" onClick={handleSave}>Save</Button>
            </div>
          </CardBody>
        </Card>
      )}

      {/* Documents List */}
      {documents.length === 0 ? (
        <Card>
          <CardBody className="p-12 text-center text-gray-500">
            <p>No documents uploaded yet</p>
          </CardBody>
        </Card>
      ) : (
        <div className="space-y-4">
          {documents.map((doc) => (
            <Card key={doc.id}>
              <CardBody className="p-6">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-3">
                      <h3 className="text-lg font-bold text-gray-900">{doc.document_name}</h3>
                      <Chip size="sm" variant="flat">
                        {doc.document_type.replace(/_/g, ' ')}
                      </Chip>
                      <Chip size="sm" color={doc.is_verified ? 'success' : 'warning'} variant="flat">
                        {doc.is_verified ? 'Verified' : 'Pending'}
                      </Chip>
                      {doc.expires_at && isExpired(doc.expires_at) && (
                        <Chip size="sm" color="danger" variant="flat">
                          Expired
                        </Chip>
                      )}
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      <div>
                        <p className="text-sm text-gray-600">Uploaded</p>
                        <p className="font-medium">
                          {new Date(doc.uploaded_at).toLocaleDateString()}
                        </p>
                      </div>
                      {doc.expires_at && (
                        <div>
                          <p className="text-sm text-gray-600">Expires</p>
                          <p className="font-medium">
                            {new Date(doc.expires_at).toLocaleDateString()}
                          </p>
                        </div>
                      )}
                      <div>
                        <p className="text-sm text-gray-600">File</p>
                        <a
                          href={doc.file_url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-blue-600 hover:underline font-medium"
                        >
                          View Document →
                        </a>
                      </div>
                    </div>

                    <div className="flex gap-2 mt-4">
                      <Button
                        size="sm"
                        color="danger"
                        variant="light"
                        onClick={() => handleDelete(doc.id)}
                      >
                        Delete
                      </Button>
                    </div>
                  </div>
                </div>
              </CardBody>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
