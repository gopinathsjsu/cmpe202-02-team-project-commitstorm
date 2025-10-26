import apiClient from './apiClient.js';

/**
 * Login user with email and password
 * @param {Object} credentials - { email, password }
 * @returns {Promise<Object>} - { token, type, id, name, email, role, status }
 */
export const login = async ({ email, password }) => {
  try {
    const response = await apiClient.post('/api/auth/login', {
      email,
      password,
    });
    
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Login failed',
      status: error.status || 500,
    };
  }
};

/**
 * Register new user with name, email and password
 * @param {Object} userData - { name, email, password }
 * @returns {Promise<Object>} - { token, type, id, name, email, role, status }
 */
export const register = async ({ name, email, password }) => {
  try {
    const response = await apiClient.post('/api/auth/register', {
      name,
      email,
      password,
    });
    
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Registration failed',
      status: error.status || 500,
    };
  }
};
