
import axios from 'axios';

// Create axios instance with base URL
// Default to localhost:8081 if env var is not set
const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081/api/v1';

const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add a request interceptor to add the auth token to every request
api.interceptors.request.use(
    (config) => {
        // Check if we're running in the browser
        if (typeof window !== 'undefined') {
            const token = localStorage.getItem('accessToken');
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
            }
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Add a response interceptor to handle 401 errors
api.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        if (error.response && error.response.status === 401) {
            // If 401, clear token and potential redirect (optional)
            if (typeof window !== 'undefined') {
                localStorage.removeItem('accessToken');
                // We could redirect to login here via window.location,
                // but prefer to handle it in the context/components
            }
        }
        return Promise.reject(error);
    }
);

export default api;
