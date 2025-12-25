
import api from '@/lib/axios';

export interface User {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
}

export interface AuthResponse {
    accessToken: string;
    message: string;
    userId: number; // Backend might return userId or user object
}

export const authService = {
    async register(data: {
        email: string;
        password: string;
        confirmPassword: string;
        firstName: string;
        lastName: string;
        role: string;
        acceptTerms: boolean;
        acceptKvkk: boolean;
    }) {
        const response = await api.post('/auth/register', data);
        return response.data;
    },

    async login(data: { email: string; password: string }) {
        const response = await api.post('/auth/login', data);
        if (response.data.data && response.data.data.accessToken) {
            localStorage.setItem('accessToken', response.data.data.accessToken);
        }
        return response.data;
    },

    async logout() {
        localStorage.removeItem('accessToken');
    },

    async getCurrentUser() {
        const response = await api.get('/users/me');
        return response.data;
    },
};
