import axios from 'axios';

// Get base URL from environment variables
const baseURL = import.meta.env.VITE_API_BASE_URL || import.meta.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

// Create axios instance
const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Store for auth token
let authToken = null;

// Load token from localStorage on initialization
const loadTokenFromStorage = () => {
  const storedToken = localStorage.getItem('auth.token');
  if (storedToken) {
    authToken = storedToken;
  }
};

// Initialize token from storage
loadTokenFromStorage();

// Helper functions for token management
export const setAuthToken = (token) => {
  authToken = token;
  if (token) {
    localStorage.setItem('auth.token', token);
  } else {
    localStorage.removeItem('auth.token');
  }
};

export const clearAuthToken = () => {
  authToken = null;
  localStorage.removeItem('auth.token');
};

// Get current token (useful for debugging)
export const getAuthToken = () => {
  return authToken;
};

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  (config) => {
    if (authToken) {
      config.headers.Authorization = `Bearer ${authToken}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error normalization and token handling
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // Handle token expiration (401 Unauthorized)
    if (error.response?.status === 401) {
      // Clear the token and redirect to login
      clearAuthToken();
      
      // Dispatch a custom event to notify the app about token expiration
      window.dispatchEvent(new CustomEvent('auth:tokenExpired'));
    }

    // Normalize error response
    const normalizedError = {
      message: 'An unexpected error occurred',
      status: error.response?.status || 500,
    };

    if (error.response?.data?.message) {
      normalizedError.message = error.response.data.message;
    } else if (error.response?.data?.error) {
      normalizedError.message = error.response.data.error;
    } else if (error.message) {
      normalizedError.message = error.message;
    }

    throw normalizedError;
  }
);

export default apiClient;
