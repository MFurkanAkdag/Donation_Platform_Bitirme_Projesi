"use client";

import { useEffect, useState } from "react";
import { Card, CardBody, CardHeader, Input, Select, SelectItem, Button, Chip } from "@heroui/react";
import { getBankAccounts, addBankAccount, updateBankAccount, deleteBankAccount } from "@/lib/mockOrgData";
import type { OrganizationBankAccount } from "@/types/organization";

const maskIBAN = (iban: string) => {
  if (iban.length <= 10) return iban;
  const first = iban.substring(0, 6);
  const last = iban.substring(iban.length - 4);
  return `${first}${'X'.repeat(iban.length - 10)}${last}`;
};

export default function OrgBankingPage() {
  const [accounts, setAccounts] = useState<OrganizationBankAccount[]>([]);
  const [isAdding, setIsAdding] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [formData, setFormData] = useState<Partial<OrganizationBankAccount>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    setAccounts(getBankAccounts());
  }, []);

  const resetForm = () => {
    setFormData({});
    setErrors({});
    setIsAdding(false);
    setEditingId(null);
  };

  const handleChange = (field: keyof OrganizationBankAccount, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    setErrors(prev => ({ ...prev, [field]: '' }));
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.bank_name?.trim()) {
      newErrors.bank_name = 'Bank name is required';
    }

    if (!formData.iban?.trim()) {
      newErrors.iban = 'IBAN is required';
    } else {
      const cleanIban = formData.iban.replace(/\s/g, '');
      if (cleanIban.startsWith('TR') && cleanIban.length !== 26) {
        newErrors.iban = 'Turkish IBAN must be 26 characters';
      }
    }

    if (!formData.currency) {
      newErrors.currency = 'Currency is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = () => {
    if (!validate()) return;

    if (editingId) {
      const updated = updateBankAccount(editingId, formData);
      if (updated) {
        setAccounts(getBankAccounts());
      }
    } else {
      addBankAccount({
        organization_id: 'org_1',
        bank_name: formData.bank_name!,
        iban: formData.iban!.replace(/\s/g, ''),
        account_holder_name: formData.account_holder_name,
        currency: formData.currency || 'TRY',
        is_primary: formData.is_primary || false,
        is_verified: false,
      });
      setAccounts(getBankAccounts());
    }
    resetForm();
  };

  const handleEdit = (account: OrganizationBankAccount) => {
    setFormData(account);
    setEditingId(account.id);
  };

  const handleDelete = (id: string) => {
    if (confirm('Are you sure you want to delete this bank account?')) {
      deleteBankAccount(id);
      setAccounts(getBankAccounts());
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">Bank Accounts</h1>
        <Button color="primary" onClick={() => setIsAdding(true)} isDisabled={isAdding || editingId !== null}>
          + Add Bank Account
        </Button>
      </div>

      {/* Add/Edit Form */}
      {(isAdding || editingId) && (
        <Card>
          <CardHeader className="pb-0 pt-6 px-6">
            <h2 className="text-xl font-bold">{editingId ? 'Edit Bank Account' : 'Add Bank Account'}</h2>
          </CardHeader>
          <CardBody className="p-6 space-y-4">
            <Input
              label="Bank Name"
              value={formData.bank_name || ''}
              onChange={(e) => handleChange('bank_name', e.target.value)}
              placeholder="e.g., Ziraat Bankası"
              isRequired
              isInvalid={!!errors.bank_name}
              errorMessage={errors.bank_name}
            />

            <Input
              label="IBAN"
              value={formData.iban || ''}
              onChange={(e) => handleChange('iban', e.target.value)}
              placeholder="TR00 0000 0000 0000 0000 0000 00"
              isRequired
              isInvalid={!!errors.iban}
              errorMessage={errors.iban}
              description="Turkish IBAN format (26 characters)"
            />

            <Input
              label="Account Holder Name"
              value={formData.account_holder_name || ''}
              onChange={(e) => handleChange('account_holder_name', e.target.value)}
            />

            <Select
              label="Currency"
              selectedKeys={formData.currency ? [formData.currency] : []}
              onSelectionChange={(keys) => handleChange('currency', Array.from(keys)[0])}
              isRequired
              isInvalid={!!errors.currency}
              errorMessage={errors.currency}
            >
              <SelectItem key="TRY">Turkish Lira (TRY)</SelectItem>
              <SelectItem key="USD">US Dollar (USD)</SelectItem>
              <SelectItem key="EUR">Euro (EUR)</SelectItem>
            </Select>

            <div className="flex items-center pt-2">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={formData.is_primary || false}
                  onChange={(e) => handleChange('is_primary', e.target.checked)}
                  className="w-4 h-4"
                />
                <span className="text-sm">Set as Primary Account</span>
              </label>
            </div>

            <div className="flex gap-2 justify-end">
              <Button variant="bordered" onClick={resetForm}>Cancel</Button>
              <Button color="primary" onClick={handleSave}>Save</Button>
            </div>
          </CardBody>
        </Card>
      )}

      {/* Accounts List */}
      {accounts.length === 0 ? (
        <Card>
          <CardBody className="p-12 text-center text-gray-500">
            <p>No bank accounts added yet</p>
          </CardBody>
        </Card>
      ) : (
        <div className="space-y-4">
          {accounts.map((account) => (
            <Card key={account.id}>
              <CardBody className="p-6">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-3">
                      <h3 className="text-lg font-bold text-gray-900">{account.bank_name}</h3>
                      {account.is_primary && (
                        <Chip size="sm" color="primary" variant="flat">Primary</Chip>
                      )}
                      <Chip size="sm" color={account.is_verified ? 'success' : 'warning'} variant="flat">
                        {account.is_verified ? 'Verified' : 'Pending Verification'}
                      </Chip>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      <div>
                        <p className="text-sm text-gray-600">IBAN</p>
                        <p className="font-mono font-medium">{maskIBAN(account.iban)}</p>
                      </div>
                      {account.account_holder_name && (
                        <div>
                          <p className="text-sm text-gray-600">Account Holder</p>
                          <p className="font-medium">{account.account_holder_name}</p>
                        </div>
                      )}
                      <div>
                        <p className="text-sm text-gray-600">Currency</p>
                        <p className="font-medium">{account.currency}</p>
                      </div>
                    </div>

                    <div className="flex gap-2 mt-4">
                      <Button size="sm" variant="flat" onClick={() => handleEdit(account)}>Edit</Button>
                      {!account.is_primary && (
                        <Button size="sm" color="danger" variant="light" onClick={() => handleDelete(account.id)}>Delete</Button>
                      )}
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
