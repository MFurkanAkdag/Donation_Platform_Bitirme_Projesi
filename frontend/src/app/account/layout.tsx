"use client";

import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import Link from "next/link";
import { Button } from "@heroui/button";

export default function AccountLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { user, isAuthenticated, loading } = useAuth();
  const router = useRouter();

  // Redirect to login if not authenticated
  useEffect(() => {
    if (!loading && !isAuthenticated) {
      router.push("/auth/login");
    }
  }, [isAuthenticated, loading, router]);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="flex flex-col md:flex-row gap-6">
            {/* Sidebar Navigation */}
            <div className="md:w-64 flex-shrink-0">
              <nav className="bg-white rounded-lg shadow p-4">
                <div className="mb-6">
                  <h3 className="text-lg font-medium text-gray-900">Account</h3>
                  <p className="text-sm text-gray-500 mt-1">
                    {user?.displayName || user?.firstName} ({user?.role})
                  </p>
                </div>
                
                <ul className="space-y-2">
                  <li>
                    <Link 
                      href="/account" 
                      className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-100 hover:text-gray-900"
                    >
                      Dashboard
                    </Link>
                  </li>
                  <li>
                    <Link 
                      href="/account/profile" 
                      className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-100 hover:text-gray-900"
                    >
                      Profile
                    </Link>
                  </li>
                  <li>
                    <Link 
                      href="/account/settings" 
                      className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-100 hover:text-gray-900"
                    >
                      Settings
                    </Link>
                  </li>
                </ul>
                
                <div className="mt-8 pt-4 border-t border-gray-200">
                  <Button 
                    color="danger" 
                    variant="flat"
                    onClick={() => {
                      // In a real app, we would call a logout function
                      // For now, we'll just redirect to the homepage
                      router.push("/");
                    }}
                    className="w-full"
                  >
                    Sign out
                  </Button>
                </div>
              </nav>
            </div>
            
            {/* Main Content */}
            <div className="flex-grow">
              {children}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}