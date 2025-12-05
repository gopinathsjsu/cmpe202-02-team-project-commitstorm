import apiClient from './apiClient.js';

/**
 * Get a User by ID
 * @param {string} userId - UUID of the user
 * @returns {Promise<Response>} User object
 */
export const getUserById = async (userId) => {
  try {
    const response = await apiClient.get(`/api/users/${userId}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch user',
      status: error.status || 500,
    };
  }
};

/**
 * Update a User's status
 * @param {string} userId - UUID of the user
 * @param {string} status - status to be updated to
 */
export const updateUserStatus = async (userId, status) => {
  try {
    const response = await apiClient.patch(`/api/users/${userId}/status`, {'status': status})
    return response.data
  } catch (error) {
    throw {
      message: error.message || 'Failed to update user',
      status: error.status || 500,
    };
  }
}