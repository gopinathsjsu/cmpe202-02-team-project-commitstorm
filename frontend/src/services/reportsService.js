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

/**
 * Get reports
 * @param {string} status - status to filter reports
 * @returns {Promise<Array>} List of Reports
 */
export const getReportsByStatus = async (status) => {
  try {
    const response = await apiClient.get(`/api/reports/status/${status}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || `Failed to fetch reports with status ${status}`,
      status: error.status || 500,
    };
  }
};

/**
 * Update Report's moderator
 * @param {string} adminId - moderator's Id
 * @param {string} reportId - Id of the reoort to be updated
 * @returns {Promise<Response>} Status of the update
 */
export const assignModerator = async (adminId, reportId) => {
  try {
    const response = await apiClient.patch(`/api/reports/${reportId}/assign-moderator?moderatorId=${adminId}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to update report',
      status: error.status || 500,
    };
  }
}

/**
 * Update report status
 * @param {string} reportId - UUID of report to update
 * @param {string} status - status to be updated to
 * @returns {Promise<Response>} Status of the update
 */
export const updateReportStatus = async (reportId, status) => {
  try {
    const response = await apiClient.patch(`api/reports/${reportId}/status`, {"status": status})
    return response.data
  } catch (error) {
    throw {
      message: error.message || 'Failed to update report status',
      status: error.status || 500,
    };
  }
}