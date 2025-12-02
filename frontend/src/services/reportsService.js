import apiClient from './apiClient.js';

/**
 * Report a listing
 * @param {Object} reportData - { reporterId, targetType, targetId, reason }
 * @returns {Promise<Object>} Report object
 */
export const reportListing = async (reportData) => {
  try {
    const response = await apiClient.post('/api/reports', reportData);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to submit report',
      status: error.status || 500,
    };
  }
};

