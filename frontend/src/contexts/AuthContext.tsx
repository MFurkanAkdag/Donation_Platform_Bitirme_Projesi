"use client";

import { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { authService, User } from "@/services/authService";
import { useRouter } from "next/navigation";

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<{ success: boolean; error?: string }>;
  register: (email: string, password: string, confirmPassword: string, firstName: string, lastName: string, role: string, acceptTerms: boolean, acceptKvkk: boolean) => Promise<{ success: boolean; error?: string }>;
  logout: () => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  // Check authentication status on mount
  useEffect(() => {
    const checkAuth = async () => {
      const token = localStorage.getItem("accessToken");
      if (token) {
        try {
          const userData = await authService.getCurrentUser();
          setUser(userData);
          setIsAuthenticated(true);
        } catch (error) {
          console.error("Auth check failed:", error);
          localStorage.removeItem("accessToken");
          setUser(null);
          setIsAuthenticated(false);
        }
      }
      setLoading(false);
    };

    checkAuth();
  }, []);

  const login = async (email: string, password: string) => {
    setLoading(true);
    try {
      const response = await authService.login({ email, password });
      // The service already sets the token in localStorage

      // Fetch user profile immediately
      const userData = await authService.getCurrentUser();

      setUser(userData);
      setIsAuthenticated(true);
      return { success: true };
    } catch (error: any) {
      console.error("Login error:", error);
      const errorMessage = error.response?.data?.message || "Login failed";
      return { success: false, error: errorMessage };
    } finally {
      setLoading(false);
    }
  };

  const register = async (email: string, password: string, confirmPassword: string, firstName: string, lastName: string, role: string, acceptTerms: boolean, acceptKvkk: boolean) => {
    setLoading(true);
    try {
      await authService.register({ email, password, confirmPassword, firstName, lastName, role, acceptTerms, acceptKvkk });
      // Depending on backend, we might be auto-logged in or need to login
      // Assuming register just creates account, let's auto-login or ask user to login
      // For now, let's try to login automatically
      try {
        const loginResp = await authService.login({ email, password });
        const userData = await authService.getCurrentUser();
        setUser(userData);
        setIsAuthenticated(true);
        return { success: true };
      } catch (loginErr) {
        // If auto-login fails, just return success for registration
        return { success: true };
      }

    } catch (error: any) {
      console.error("Registration error:", error);
      const errorMessage = error.response?.data?.message || "Registration failed";
      return { success: false, error: errorMessage };
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setIsAuthenticated(false);
    router.push("/auth/login");
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated,
        login,
        register,
        logout,
        loading
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
