"use client";

import { useState, useEffect } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@heroui/button";
import { Input, Textarea } from "@heroui/react";
import { Card, CardBody, CardHeader } from "@heroui/react";
import { mockAuthService } from "@/lib/mockAuth";

export default function ProfilePage() {
  const { user } = useAuth();
  const [profile, setProfile] = useState<any>({});
  const [preferences, setPreferences] = useState<any>({});
  const [isSaving, setIsSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  // Load profile data on mount
  useEffect(() => {
    if (user) {
      const userProfile = mockAuthService.getUserProfile(user.id);
      const userPreferences = mockAuthService.getUserPreferences(user.id);
      setProfile(userProfile);
      setPreferences(userPreferences);
    }
  }, [user]);

  const handleProfileChange = (field: string, value: string) => {
    setProfile((prev: any) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    setSaveSuccess(false);

    try {
      // In a real app, this would call an API
      // For demo, we'll just update the mock service
      if (user) {
        mockAuthService.updateUserProfile(user.id, profile);
        setSaveSuccess(true);
        
        // Hide success message after 3 seconds
        setTimeout(() => setSaveSuccess(false), 3000);
      }
    } catch (error) {
      console.error("Failed to update profile", error);
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Your Profile</h1>
        <p className="mt-1 text-sm text-gray-600">
          Manage your personal information and preferences.
        </p>
      </div>

      <Card>
        <CardHeader className="pb-4">
          <h3 className="text-lg font-medium text-gray-900">Personal Information</h3>
        </CardHeader>
        <CardBody className="p-6">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <div>
                <Input
                  label="First Name"
                  value={profile.firstName || ""}
                  onChange={(e) => handleProfileChange("firstName", e.target.value)}
                  placeholder="John"
                />
              </div>
              
              <div>
                <Input
                  label="Last Name"
                  value={profile.lastName || ""}
                  onChange={(e) => handleProfileChange("lastName", e.target.value)}
                  placeholder="Doe"
                />
              </div>
            </div>
            
            <div>
              <Input
                label="Display Name"
                value={profile.displayName || ""}
                onChange={(e) => handleProfileChange("displayName", e.target.value)}
                placeholder="John D."
                description="This is how your name will appear to others"
              />
            </div>
            
            <div>
              <Input
                label="Avatar URL"
                value={profile.avatarUrl || ""}
                onChange={(e) => handleProfileChange("avatarUrl", e.target.value)}
                placeholder="https://example.com/avatar.jpg"
              />
            </div>
            
            <div>
              <Textarea
                label="Bio"
                value={profile.bio || ""}
                onChange={(e) => handleProfileChange("bio", e.target.value)}
                placeholder="Tell us about yourself..."
                minRows={3}
              />
            </div>
            
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <div>
                <Input
                  label="Preferred Language"
                  value={profile.preferredLanguage || ""}
                  onChange={(e) => handleProfileChange("preferredLanguage", e.target.value)}
                  placeholder="en"
                />
              </div>
              
              <div>
                <Input
                  label="Timezone"
                  value={profile.timezone || ""}
                  onChange={(e) => handleProfileChange("timezone", e.target.value)}
                  placeholder="UTC"
                />
              </div>
            </div>
            
            <div className="flex items-center justify-end">
              {saveSuccess && (
                <div className="mr-4 text-sm text-green-600">
                  Profile updated successfully!
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
    </div>
  );
}