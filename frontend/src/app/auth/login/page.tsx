"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@heroui/button";
import { Input } from "@heroui/react";
import { Card, CardBody, CardHeader } from "@heroui/react";
import { useAuth } from "@/contexts/AuthContext";
import Link from "next/link";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();
  const { login } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      const response = await login(email, password);
      if (response.success) {
        router.push("/account");
      } else {
        setError(response.error || "Invalid credentials");
      }
    } catch (err) {
      setError("An unexpected error occurred");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <Card className="w-full max-w-md">
        <CardHeader className="pb-0 pt-6 px-6">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-gray-900">Sign in to your account</h2>
            <p className="mt-2 text-sm text-gray-600">
              Or{" "}
              <Link href="/auth/register" className="font-medium text-blue-600 hover:text-blue-500">
                create a new account
              </Link>
            </p>
          </div>
        </CardHeader>
        <CardBody className="p-6">
          {error && (
            <div className="mb-4 rounded-md bg-red-50 p-4">
              <div className="flex">
                <div className="text-sm font-medium text-red-800">{error}</div>
              </div>
            </div>
          )}
          
          <form onSubmit={handleSubmit} className="space-y-6">
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
              />
            </div>
            
            <div className="flex items-center justify-between">
              <div className="text-sm">
                <Link href="/auth/forgot-password" className="font-medium text-blue-600 hover:text-blue-500">
                  Forgot your password?
                </Link>
              </div>
            </div>
            
            <div>
              <Button
                type="submit"
                color="primary"
                isLoading={isLoading}
                className="w-full"
              >
                Sign in
              </Button>
            </div>
          </form>
          
          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">Demo Credentials</span>
              </div>
            </div>
            
            <div className="mt-4 bg-blue-50 p-4 rounded-md">
              <p className="text-sm text-gray-700">
                <strong>Email:</strong> admin@example.com<br />
                <strong>Password:</strong> Admin123!
                <br /> <br />
                <strong>Email:</strong> foundation@example.com<br />
                <strong>Password:</strong> Foundation123!
              </p>
            </div>
          </div>
        </CardBody>
      </Card>
    </div>
  );
}