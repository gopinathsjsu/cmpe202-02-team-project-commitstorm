import axios from 'axios';

// Get base URL from environment variables
// Direct connection to backend on localhost:8080
const getBaseURL = () => {
  // If VITE_API_BASE_URL is set, use it (allows override for different environments)
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL;
  }
  // Default to localhost:8080 for development and production fallback
  return 'http://alb-cmpmarket-public-1403545222.us-west-2.elb.amazonaws.com';
};

const baseURL = getBaseURL();

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
  localStorage.removeItem('auth.tokenType');
};

export const getAuthToken = () => {
  return authToken;
};

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  (config) => {
    // Always check localStorage for token (in case token was set after module load)
    const token = authToken || localStorage.getItem('auth.token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      // Update the authToken variable if it was null but token exists in storage
      if (!authToken && token) {
        authToken = token;
      }
    } else {
      console.log('No auth token found for request:', config.url);
    }
    console.log('API Request:', config.method?.toUpperCase(), config.baseURL + config.url);
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
