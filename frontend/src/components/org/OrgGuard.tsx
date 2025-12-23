"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";

export default function OrgGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const { user, isAuthenticated, loading } = useAuth();

  useEffect(() => {
    if (loading) return;

    if (!isAuthenticated) {
      router.push('/auth/login');
      return;
    }

    const isFoundation = ['foundation', 'association', 'ngo'].includes(user?.role?.toLowerCase() || '');
    if (!isFoundation) {
      router.push('/account');
    }
  }, [isAuthenticated, loading, user, router]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  const isFoundation = ['foundation', 'association', 'ngo'].includes(user?.role?.toLowerCase() || '');
  if (!isAuthenticated || !isFoundation) {
    return null;
  }

  return <>{children}</>;
}
