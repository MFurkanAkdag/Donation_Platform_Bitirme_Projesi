"use client";

import { Card, CardBody, Button } from "@heroui/react";
import { Input, Select, SelectItem } from "@heroui/react";

interface DocumentData {
  document_type: string;
  document_name: string;
  file_url: string;
  expires_at: string;
}

interface Step2Data {
  // Bank Account
  bank_name: string;
  iban: string;
  currency: string;
  // Documents
  documents: DocumentData[];
}

interface OrganizationStep2Props {
  data: Step2Data;
  onChange: (data: Step2Data) => void;
  errors: Record<string, string>;
}

export default function OrganizationStep2({ data, onChange, errors }: OrganizationStep2Props) {
  const handleChange = (field: keyof Step2Data, value: any) => {
    onChange({ ...data, [field]: value });
  };

  const addDocument = () => {
    handleChange('documents', [
      ...data.documents,
      { document_type: '', document_name: '', file_url: '', expires_at: '' }
    ]);
  };

  const removeDocument = (index: number) => {
    handleChange('documents', data.documents.filter((_, i) => i !== index));
  };

  const updateDocument = (index: number, field: keyof DocumentData, value: string) => {
    const updated = [...data.documents];
    updated[index] = { ...updated[index], [field]: value };
    handleChange('documents', updated);
  };

  return (
    <div className="space-y-6">
      {/* Bank Account */}
      <Card>
        <CardBody className="p-6">
          <h3 className="text-xl font-bold text-gray-900 mb-4">Bank Account Information</h3>
          <div className="space-y-4">
            <Input
              label="Bank Name"
              value={data.bank_name}
              onChange={(e) => handleChange('bank_name', e.target.value)}
              placeholder="e.g., Ziraat Bankası"
              isRequired
              isInvalid={!!errors.bank_name}
              errorMessage={errors.bank_name}
            />

            <Input
              label="IBAN"
              value={data.iban}
              onChange={(e) => handleChange('iban', e.target.value)}
              placeholder="TR00 0000 0000 0000 0000 0000 00"
              isRequired
              isInvalid={!!errors.iban}
              errorMessage={errors.iban}
              description="Turkish IBAN format (26 characters)"
            />

            <Select
              label="Currency"
              selectedKeys={data.currency ? [data.currency] : []}
              onSelectionChange={(keys) => handleChange('currency', Array.from(keys)[0] as string)}
              isRequired
              isInvalid={!!errors.currency}
              errorMessage={errors.currency}
            >
              <SelectItem key="TRY">Turkish Lira (TRY)</SelectItem>
              <SelectItem key="USD">US Dollar (USD)</SelectItem>
              <SelectItem key="EUR">Euro (EUR)</SelectItem>
            </Select>
          </div>
        </CardBody>
      </Card>

      {/* Documents */}
      <Card>
        <CardBody className="p-6">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-xl font-bold text-gray-900">Required Documents</h3>
            <Button
              color="primary"
              variant="flat"
              size="sm"
              onClick={addDocument}
            >
              Add Document
            </Button>
          </div>

          {errors.documents && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">
              {errors.documents}
            </div>
          )}

          <div className="space-y-4">
            {data.documents.map((doc, index) => (
              <Card key={index} className="bg-gray-50">
                <CardBody className="p-4">
                  <div className="flex justify-between items-start mb-3">
                    <h4 className="font-semibold text-gray-700">Document {index + 1}</h4>
                    {data.documents.length > 1 && (
                      <Button
                        color="danger"
                        variant="light"
                        size="sm"
                        onClick={() => removeDocument(index)}
                      >
                        Remove
                      </Button>
                    )}
                  </div>

                  <div className="space-y-3">
                    <Select
                      label="Document Type"
                      selectedKeys={doc.document_type ? [doc.document_type] : []}
                      onSelectionChange={(keys) => updateDocument(index, 'document_type', Array.from(keys)[0] as string)}
                      isRequired
                      size="sm"
                      isInvalid={!!errors[`documents.${index}.document_type`]}
                      errorMessage={errors[`documents.${index}.document_type`]}
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
                      value={doc.document_name}
                      onChange={(e) => updateDocument(index, 'document_name', e.target.value)}
                      placeholder="e.g., 2024 Registration Certificate"
                      isRequired
                      size="sm"
                      isInvalid={!!errors[`documents.${index}.document_name`]}
                      errorMessage={errors[`documents.${index}.document_name`]}
                    />

                    <Input
                      label="File URL"
                      value={doc.file_url}
                      onChange={(e) => updateDocument(index, 'file_url', e.target.value)}
                      placeholder="https://example.com/document.pdf"
                      isRequired
                      size="sm"
                      isInvalid={!!errors[`documents.${index}.file_url`]}
                      errorMessage={errors[`documents.${index}.file_url`]}
                      description="Upload to cloud storage and paste URL (e.g., Drive, Dropbox)"
                    />

                    <Input
                      label="Expiration Date (Optional)"
                      type="date"
                      value={doc.expires_at}
                      onChange={(e) => updateDocument(index, 'expires_at', e.target.value)}
                      size="sm"
                    />
                  </div>
                </CardBody>
              </Card>
            ))}

            {data.documents.length === 0 && (
              <div className="text-center py-8 text-gray-500">
                <p className="mb-2">No documents added yet</p>
                <p className="text-sm">Click "Add Document" to upload required documents</p>
              </div>
            )}
          </div>
        </CardBody>
      </Card>
    </div>
  );
}

export type { Step2Data, DocumentData };
