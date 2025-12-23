"use client";

import { useState, useEffect } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@heroui/button";
import { Switch } from "@heroui/react";
import { Card, CardBody, CardHeader } from "@heroui/react";
import { Select, SelectItem } from "@heroui/react";
import { mockAuthService } from "@/lib/mockAuth";

export default function SettingsPage() {
  const { user } = useAuth();
  const [preferences, setPreferences] = useState<any>({});
  const [isSaving, setIsSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  // Load preferences data on mount
  useEffect(() => {
    if (user) {
      const userPreferences = mockAuthService.getUserPreferences(user.id);
      setPreferences(userPreferences);
    }
  }, [user]);

  const handlePreferenceChange = (field: string, value: any) => {
    setPreferences((prev: any) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    setSaveSuccess(false);

    try {
      // In a real app, this would call an API
      // For demo, we'll just update the mock service
      if (user) {
        mockAuthService.updateUserPreferences(user.id, preferences);
        setSaveSuccess(true);
        
        // Hide success message after 3 seconds
        setTimeout(() => setSaveSuccess(false), 3000);
      }
    } catch (error) {
      console.error("Failed to update preferences", error);
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Settings</h1>
        <p className="mt-1 text-sm text-gray-600">
          Manage your notification and privacy preferences.
        </p>
      </div>

      <Card>
        <CardHeader className="pb-4">
          <h3 className="text-lg font-medium text-gray-900">Notification Preferences</h3>
        </CardHeader>
        <CardBody className="p-6">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="text-base font-medium text-gray-900">Email Notifications</h4>
                  <p className="text-sm text-gray-500">
                    Receive email notifications about your donations and campaign updates.
                  </p>
                </div>
                <Switch
                  isSelected={preferences.emailNotifications ?? true}
                  onValueChange={(value) => handlePreferenceChange("emailNotifications", value)}
                />
              </div>
              
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="text-base font-medium text-gray-900">SMS Notifications</h4>
                  <p className="text-sm text-gray-500">
                    Receive SMS notifications for important updates.
                  </p>
                </div>
                <Switch
                  isSelected={preferences.smsNotifications ?? false}
                  onValueChange={(value) => handlePreferenceChange("smsNotifications", value)}
                />
              </div>
            </div>
            
            <div className="pt-4">
              <h3 className="text-lg font-medium text-gray-900">Privacy Settings</h3>
              <div className="mt-4 space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Donation Visibility
                  </label>
                  <Select
                    selectedKeys={[preferences.donationVisibility || "public"]}
                    onSelectionChange={(keys) => {
                      const value = Array.from(keys)[0] as string;
                      handlePreferenceChange("donationVisibility", value);
                    }}
                    className="w-full max-w-xs"
                  >
                    <SelectItem key="public">
                      Public
                    </SelectItem>
                    <SelectItem key="anonymous">
                      Anonymous
                    </SelectItem>
                    <SelectItem key="private">
                      Private
                    </SelectItem>
                  </Select>
                  <p className="mt-1 text-sm text-gray-500">
                    Control who can see your donation history.
                  </p>
                </div>
                
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="text-base font-medium text-gray-900">Show in Donor List</h4>
                    <p className="text-sm text-gray-500">
                      Allow your name to appear in donor lists for campaigns you support.
                    </p>
                  </div>
                  <Switch
                    isSelected={preferences.showInDonorList ?? true}
                    onValueChange={(value) => handlePreferenceChange("showInDonorList", value)}
                  />
                </div>
              </div>
            </div>
            
            <div className="flex items-center justify-end">
              {saveSuccess && (
                <div className="mr-4 text-sm text-green-600">
                  Settings updated successfully!
                </div>
              )}
              <Button
                type="submit"
                color="primary"
                isLoading={isSaving}
              >
                Save Changes
              </Button>
            </div>
          </form>
        </CardBody>
      </Card>
      
      <Card>
        <CardHeader className="pb-4">
          <h3 className="text-lg font-medium text-gray-900">Security</h3>
        </CardHeader>
        <CardBody className="p-6">
          <div className="space-y-4">
            <div>
              <h4 className="text-base font-medium text-gray-900">Password</h4>
              <p className="text-sm text-gray-500">
                Change your password to keep your account secure.
              </p>
              <div className="mt-2">
                <Button 
                  color="primary" 
                  variant="flat"
                  onPress={() => {
                    // In a real app, this would navigate to a password change page
                    alert("In a real application, this would take you to a password change page.");
                  }}
                >
                  Change Password
                </Button>
              </div>
            </div>
            
            <div className="pt-4 border-t border-gray-200">
              <h4 className="text-base font-medium text-gray-900">Two-Factor Authentication</h4>
              <p className="text-sm text-gray-500">
                Add an extra layer of security to your account.
              </p>
              <div className="mt-2">
                <Button 
                  color="primary" 
                  variant="flat"
                  onPress={() => {
                    // In a real app, this would enable 2FA
                    alert("In a real application, this would enable two-factor authentication.");
                  }}
                >
                  Enable Two-Factor Authentication
                </Button>
              </div>
            </div>
          </div>
        </CardBody>
      </Card>
    </div>
  );
}