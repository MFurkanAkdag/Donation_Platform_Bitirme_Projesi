"use client";

import { useEffect, useState } from "react";
import { Card, CardBody, CardHeader, Input, Select, SelectItem, Button, Chip, Accordion, AccordionItem } from "@heroui/react";
import { getContacts, addContact, updateContact, deleteContact } from "@/lib/mockOrgData";
import type { OrganizationContact } from "@/types/organization";

export default function OrgContactPage() {
  const [contacts, setContacts] = useState<OrganizationContact[]>([]);
  const [isAdding, setIsAdding] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [formData, setFormData] = useState<Partial<OrganizationContact>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    setContacts(getContacts());
  }, []);

  const resetForm = () => {
    setFormData({});
    setErrors({});
    setIsAdding(false);
    setEditingId(null);
  };

  const handleChange = (field: keyof OrganizationContact, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    setErrors(prev => ({ ...prev, [field]: '' }));
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.email?.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Invalid email format';
    }

    if (!formData.phone?.trim()) {
      newErrors.phone = 'Phone is required';
    }

    if (!formData.city?.trim()) {
      newErrors.city = 'City is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = () => {
    if (!validate()) return;

    if (editingId) {
      const updated = updateContact(editingId, formData);
      if (updated) {
        setContacts(getContacts());
      }
    } else {
      addContact({
        organization_id: 'org_1',
        contact_type: formData.contact_type || 'other',
        is_primary: formData.is_primary || false,
        email: formData.email!,
        phone: formData.phone!,
        country: formData.country,
        city: formData.city,
        district: formData.district,
        address_line1: formData.address_line1,
        address_line2: formData.address_line2,
      });
      setContacts(getContacts());
    }
    resetForm();
  };

  const handleEdit = (contact: OrganizationContact) => {
    setFormData(contact);
    setEditingId(contact.id);
  };

  const handleDelete = (id: string) => {
    if (confirm('Are you sure you want to delete this contact?')) {
      deleteContact(id);
      setContacts(getContacts());
    }
  };

  const primaryContact = contacts.find(c => c.is_primary);
  const otherContacts = contacts.filter(c => !c.is_primary);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">Contacts</h1>
        <Button color="primary" onClick={() => setIsAdding(true)} isDisabled={isAdding || editingId !== null}>
          + Add Contact
        </Button>
      </div>

      {/* Add/Edit Form */}
      {(isAdding || editingId) && (
        <Card>
          <CardHeader className="pb-0 pt-6 px-6">
            <h2 className="text-xl font-bold">{editingId ? 'Edit Contact' : 'Add New Contact'}</h2>
          </CardHeader>
          <CardBody className="p-6 space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Select
                label="Contact Type"
                selectedKeys={formData.contact_type ? [formData.contact_type] : []}
                onSelectionChange={(keys) => handleChange('contact_type', Array.from(keys)[0])}
              >
                <SelectItem key="head_office">Head Office</SelectItem>
                <SelectItem key="branch">Branch</SelectItem>
                <SelectItem key="billing">Billing</SelectItem>
                <SelectItem key="technical">Technical</SelectItem>
                <SelectItem key="other">Other</SelectItem>
              </Select>

              <div className="flex items-center pt-6">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formData.is_primary || false}
                    onChange={(e) => handleChange('is_primary', e.target.checked)}
                    className="w-4 h-4"
                  />
                  <span className="text-sm">Primary Contact</span>
                </label>
              </div>
            </div>

            <Input
              label="Email"
              type="email"
              value={formData.email || ''}
              onChange={(e) => handleChange('email', e.target.value)}
              isRequired
              isInvalid={!!errors.email}
              errorMessage={errors.email}
            />

            <Input
              label="Phone"
              value={formData.phone || ''}
              onChange={(e) => handleChange('phone', e.target.value)}
              placeholder="+90 555 123 4567"
              isRequired
              isInvalid={!!errors.phone}
              errorMessage={errors.phone}
            />

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <Input
                label="Country"
                value={formData.country || ''}
                onChange={(e) => handleChange('country', e.target.value)}
              />
              <Input
                label="City"
                value={formData.city || ''}
                onChange={(e) => handleChange('city', e.target.value)}
                isRequired
                isInvalid={!!errors.city}
                errorMessage={errors.city}
              />
              <Input
                label="District"
                value={formData.district || ''}
                onChange={(e) => handleChange('district', e.target.value)}
              />
            </div>

            <Input
              label="Address Line 1"
              value={formData.address_line1 || ''}
              onChange={(e) => handleChange('address_line1', e.target.value)}
            />

            <Input
              label="Address Line 2"
              value={formData.address_line2 || ''}
              onChange={(e) => handleChange('address_line2', e.target.value)}
            />

            <div className="flex gap-2 justify-end">
              <Button variant="bordered" onClick={resetForm}>Cancel</Button>
              <Button color="primary" onClick={handleSave}>Save</Button>
            </div>
          </CardBody>
        </Card>
      )}

      {/* Primary Contact */}
      {primaryContact && (
        <Card>
          <CardHeader className="pb-0 pt-6 px-6">
            <div className="flex items-center justify-between w-full">
              <h2 className="text-xl font-bold">Primary Contact</h2>
              <Chip color="primary" variant="flat">Primary</Chip>
            </div>
          </CardHeader>
          <CardBody className="p-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-gray-600">Type</p>
                <p className="font-medium">{primaryContact.contact_type.replace('_', ' ')}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Email</p>
                <p className="font-medium">{primaryContact.email}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Phone</p>
                <p className="font-medium">{primaryContact.phone}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Location</p>
                <p className="font-medium">{primaryContact.city}{primaryContact.country ? `, ${primaryContact.country}` : ''}</p>
              </div>
              {primaryContact.address_line1 && (
                <div className="md:col-span-2">
                  <p className="text-sm text-gray-600">Address</p>
                  <p className="font-medium">{primaryContact.address_line1}{primaryContact.address_line2 ? `, ${primaryContact.address_line2}` : ''}</p>
                </div>
              )}
            </div>
            <div className="flex gap-2 mt-4">
              <Button size="sm" variant="flat" onClick={() => handleEdit(primaryContact)}>Edit</Button>
            </div>
          </CardBody>
        </Card>
      )}

      {/* Other Contacts */}
      {otherContacts.length > 0 && (
        <Card>
          <CardHeader className="pb-0 pt-6 px-6">
            <h2 className="text-xl font-bold">Additional Contacts</h2>
          </CardHeader>
          <CardBody className="p-6">
            <Accordion>
              {otherContacts.map((contact) => (
                <AccordionItem key={contact.id} title={`${contact.contact_type.replace('_', ' ')} - ${contact.email}`}>
                  <div className="space-y-3 pb-4">
                    <div className="grid grid-cols-2 gap-3">
                      <div>
                        <p className="text-sm text-gray-600">Phone</p>
                        <p className="font-medium">{contact.phone}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Location</p>
                        <p className="font-medium">{contact.city}</p>
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <Button size="sm" variant="flat" onClick={() => handleEdit(contact)}>Edit</Button>
                      <Button size="sm" color="danger" variant="light" onClick={() => handleDelete(contact.id)}>Delete</Button>
                    </div>
                  </div>
                </AccordionItem>
              ))}
            </Accordion>
          </CardBody>
        </Card>
      )}
    </div>
  );
}
