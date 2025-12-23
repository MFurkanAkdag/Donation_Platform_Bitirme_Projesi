// Mock authentication service for demonstration purposes
// This will be replaced with real API calls in the future

export interface User {
  id: string;
  email: string;
  role: 'admin' | 'donor' | 'foundation' | 'beneficiary';
  firstName: string;
  lastName: string;
  displayName: string;
  avatarUrl?: string;
}

// Hardcoded admin user for demo purposes
const mockAdminUser: User = {
  id: '1',
  email: 'admin@example.com',
  role: 'admin',
  firstName: 'Admin',
  lastName: 'User',
  displayName: 'Admin User',
  avatarUrl: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop&crop=face',
};

// Foundation test account
const mockFoundationUser: User = {
  id: 'user_foundation_1',
  email: 'foundation@example.com',
  role: 'foundation',
  firstName: 'Hope',
  lastName: 'Foundation',
  displayName: 'Hope Foundation',
};

// Mock user profiles
const mockUserProfiles: Record<string, any> = {
  '1': {
    firstName: 'Admin',
    lastName: 'User',
    displayName: 'Admin User',
    bio: 'Administrator of the Donation Platform',
    preferredLanguage: 'en',
    timezone: 'UTC',
  }
};

// Mock user preferences
const mockUserPreferences: Record<string, any> = {
  '1': {
    emailNotifications: true,
    smsNotifications: false,
    donationVisibility: 'public',
    showInDonorList: true,
  }
};

class MockAuthService {
  private currentUser: User | null = null;
  private isAuthenticated = false;

  // Login with email and password
  async login(email: string, password: string): Promise<{ success: boolean; user?: User; error?: string }> {
    // Check admin credentials
    if (email === 'admin@example.com' && password === 'Admin123!') {
      this.currentUser = mockAdminUser;
      this.isAuthenticated = true;
      if (typeof window !== 'undefined') {
        localStorage.setItem('mockAuthUser', JSON.stringify(mockAdminUser));
      }
      return { success: true, user: mockAdminUser };
    }
    
    // Check foundation credentials
    if (email === 'foundation@example.com' && password === 'Foundation123!') {
      this.currentUser = mockFoundationUser;
      this.isAuthenticated = true;
      if (typeof window !== 'undefined') {
        localStorage.setItem('mockAuthUser', JSON.stringify(mockFoundationUser));
      }
      return { success: true, user: mockFoundationUser };
    }
    
    return { success: false, error: 'Invalid credentials' };
  }

  // Register a new user (demo only)
  async register(email: string, password: string, firstName: string, lastName: string): Promise<{ success: boolean; user?: User; error?: string }> {
    // For demo purposes, we'll just simulate registration
    const newUser: User = {
      id: '2',
      email,
      role: 'donor',
      firstName,
      lastName,
      displayName: `${firstName} ${lastName}`,
    };
    
    this.currentUser = newUser;
    this.isAuthenticated = true;
    
    if (typeof window !== 'undefined') {
      localStorage.setItem('mockAuthUser', JSON.stringify(newUser));
    }
    
    return { success: true, user: newUser };
  }

  // Logout
  logout(): void {
    this.currentUser = null;
    this.isAuthenticated = false;
    if (typeof window !== 'undefined') {
      localStorage.removeItem('mockAuthUser');
    }
  }

  // Check if user is authenticated
  getCurrentUser(): User | null {
    // Check localStorage for persisted user
    if (!this.currentUser && typeof window !== 'undefined') {
      const storedUser = localStorage.getItem('mockAuthUser');
      if (storedUser) {
        this.currentUser = JSON.parse(storedUser);
        this.isAuthenticated = true;
      }
    }
    
    return this.currentUser;
  }

  // Check authentication status
  isAuthenticatedUser(): boolean {
    return this.getCurrentUser() !== null;
  }

  // Get user profile
  getUserProfile(userId: string) {
    return mockUserProfiles[userId] || {};
  }

  // Update user profile
  updateUserProfile(userId: string, profile: any) {
    mockUserProfiles[userId] = { ...mockUserProfiles[userId], ...profile };
    return mockUserProfiles[userId];
  }

  // Get user preferences
  getUserPreferences(userId: string) {
    return mockUserPreferences[userId] || {};
  }

  // Update user preferences
  updateUserPreferences(userId: string, preferences: any) {
    mockUserPreferences[userId] = { ...mockUserPreferences[userId], ...preferences };
    return mockUserPreferences[userId];
  }
}

// Export singleton instance
export const mockAuthService = new MockAuthService();