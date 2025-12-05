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

/**
 * Logout user
 * @returns {Promise<void>}
 */
export const logout = async () => {
  try {
    await apiClient.post('/api/auth/logout');
  } catch (error) {
    // Even if logout API fails, we should still clear local auth
    console.error('Logout API error:', error);
    // Don't throw - we still want to clear local state
  }
};
