"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@heroui/button";
import { Input, Tabs, Tab } from "@heroui/react";
import { Card, CardBody, CardHeader } from "@heroui/react";
import { useAuth } from "@/contexts/AuthContext";
import Link from "next/link";

export default function RegisterPage() {
  const [registrationType, setRegistrationType] = useState<"user" | "organization">("user");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();
  const { register } = useAuth();

  const validateForm = () => {
    if (!firstName || !lastName) {
      setError("First name and last name are required");
      return false;
    }
    
    if (!email) {
      setError("Email is required");
      return false;
    }
    
    if (!/\S+@\S+\.\S+/.test(email)) {
      setError("Email address is invalid");
      return false;
    }
    
    if (!password) {
      setError("Password is required");
      return false;
    }
    
    if (password.length < 8) {
      setError("Password must be at least 8 characters long");
      return false;
    }
    
    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return false;
    }
    
    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    setIsLoading(true);
    setError("");

    try {
      const response = await register(email, password, firstName, lastName);
      if (response.success) {
        setSuccess(true);
        // Redirect to account page after a short delay
        setTimeout(() => {
          router.push("/account");
        }, 2000);
      } else {
        setError(response.error || "Registration failed");
      }
    } catch (err) {
      setError("An unexpected error occurred");
    } finally {
      setIsLoading(false);
    }
  };

  if (success) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <Card className="w-full max-w-md">
          <CardBody className="p-6 text-center">
            <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100">
              <svg className="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h2 className="mt-4 text-lg font-medium text-gray-900">Registration Successful!</h2>
            <p className="mt-2 text-sm text-gray-600">
              Welcome {firstName}! You will be redirected to your account page shortly.
            </p>
            <div className="mt-4">
              <Button 
                color="primary" 
                onClick={() => router.push("/account")}
                className="w-full"
              >
                Go to Account Now
              </Button>
            </div>
          </CardBody>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <Card className="w-full max-w-md">
        <CardHeader className="pb-0 pt-6 px-6">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-gray-900">Create a new account</h2>
            <p className="mt-2 text-sm text-gray-600">
              Or{" "}
              <Link href="/auth/login" className="font-medium text-blue-600 hover:text-blue-500">
                sign in to your existing account
              </Link>
            </p>
          </div>
        </CardHeader>
        <CardBody className="px-6 pt-4 pb-2">
          <Tabs
            selectedKey={registrationType}
            onSelectionChange={(key) => setRegistrationType(key as "user" | "organization")}
            fullWidth
            color="primary"
          >
            <Tab key="user" title="Register as User" />
            <Tab key="organization" title="Register as Organization" />
          </Tabs>
        </CardBody>
        <CardBody className="p-6 pt-2">
          {registrationType === "organization" ? (
            <div className="space-y-4">
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <h3 className="font-semibold text-blue-900 mb-2">Organization Registration</h3>
                <p className="text-sm text-blue-800 mb-3">
                  Register your organization to create campaigns and receive donations.
                </p>
                <Button
                  color="primary"
                  onClick={() => router.push('/auth/register/organization')}
                  className="w-full"
                >
                  Start Organization Registration
                </Button>
              </div>
              <div className="text-center">
                <Button
                  variant="light"
                  onClick={() => setRegistrationType("user")}
                  className="text-gray-600"
                >
                  ← Back to User Registration
                </Button>
              </div>
            </div>
          ) : (
            <>
              {error && (
                <div className="mb-4 rounded-md bg-red-50 p-4">
                  <div className="flex">
                    <div className="text-sm font-medium text-red-800">{error}</div>
                  </div>
                </div>
              )}
              
              <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Input
                  label="First Name"
                  type="text"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  placeholder="John"
                  required
                  isRequired
                  className="w-full"
                />
              </div>
              
              <div>
                <Input
                  label="Last Name"
                  type="text"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  placeholder="Doe"
                  required
                  isRequired
                  className="w-full"
                />
              </div>
            </div>
            
            <div>
              <Input
                label="Email address"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                required
                isRequired
                className="w-full"
              />
            </div>
            
            <div>
              <Input
                label="Password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
                isRequired
                className="w-full"
                description="Must be at least 8 characters with uppercase, lowercase, and number"
              />
            </div>
            
            <div>
              <Input
                label="Confirm Password"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="••••••••"
                required
                isRequired
                className="w-full"
              />
            </div>
            
            <div className="flex items-center">
              <input
                id="terms"
                name="terms"
                type="checkbox"
                required
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label htmlFor="terms" className="ml-2 block text-sm text-gray-900">
                I agree to the{" "}
                <Link href="/terms-of-service" className="text-blue-600 hover:text-blue-500">
                  Terms of Service
                </Link>{" "}
                and{" "}
                <Link href="/privacy-policy" className="text-blue-600 hover:text-blue-500">
                  Privacy Policy
                </Link>
              </label>
            </div>
            
            <div>
              <Button
                type="submit"
                color="primary"
                isLoading={isLoading}
                className="w-full"
              >
                Create Account
              </Button>
            </div>
              </form>
            </>
          )}
        </CardBody>
      </Card>
    </div>
  );
}