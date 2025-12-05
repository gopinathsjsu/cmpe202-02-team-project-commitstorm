import apiClient from './apiClient.js';

/**
 * Request to buy a listing (buyer action)
 * @param {string} listingId - Listing ID
 * @param {string} buyerId - Buyer ID
 * @returns {Promise<Object>} Transaction object
 */
export const requestToBuy = async (listingId, buyerId) => {
  try {
    const response = await apiClient.post('/api/transactions/request-to-buy', null, {
      params: { listingId, buyerId }
    });
    return response.data;
  } catch (error) {
    throw {
      message: error.response?.data?.message || error.message || 'Failed to request purchase',
      status: error.response?.status || 500,
    };
  }
};

/**
 * Mark transaction as sold (seller action - accepts purchase request)
 * @param {string} transactionId - Transaction ID
 * @param {string} sellerId - Seller ID
 * @returns {Promise<Object>} Updated transaction object
 */
export const markAsSold = async (transactionId, sellerId) => {
  try {
    const response = await apiClient.patch(`/api/transactions/${transactionId}/mark-sold`, null, {
      params: { sellerId }
    });
    return response.data;
  } catch (error) {
    throw {
      message: error.response?.data?.message || error.message || 'Failed to confirm transaction',
      status: error.response?.status || 500,
    };
  }
};

/**
 * Reject purchase request (seller action)
 * @param {string} transactionId - Transaction ID
 * @param {string} sellerId - Seller ID
 * @returns {Promise<Object>} Updated transaction object
 */
export const rejectRequest = async (transactionId, sellerId) => {
  try {
    const response = await apiClient.patch(`/api/transactions/${transactionId}/reject`, null, {
      params: { sellerId }
    });
    return response.data;
  } catch (error) {
    throw {
      message: error.response?.data?.message || error.message || 'Failed to reject request',
      status: error.response?.status || 500,
    };
  }
};

/**
 * Get transaction by listing ID
 * @param {string} listingId - Listing ID
 * @returns {Promise<Object|null>} Transaction object or null if not found
 */
export const getTransactionByListingId = async (listingId) => {
  try {
    const response = await apiClient.get(`/api/transactions/listing/${listingId}`);
    return response.data;
  } catch (error) {
    if (error.response?.status === 404) {
      return null;
    }
    throw {
      message: error.response?.data?.message || error.message || 'Failed to fetch transaction',
      status: error.response?.status || 500,
    };
  }
};

/**
 * Get transaction by ID
 * @param {string} transactionId - Transaction ID
 * @returns {Promise<Object>} Transaction object
 */
export const getTransactionById = async (transactionId) => {
  try {
    const response = await apiClient.get(`/api/transactions/${transactionId}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.response?.data?.message || error.message || 'Failed to fetch transaction',
      status: error.response?.status || 500,
    };
  }
};

/**
 * Get transactions by buyer ID
 * @param {string} buyerId - Buyer ID
 * @returns {Promise<Array>} Array of transaction objects
 */
export const getTransactionsByBuyer = async (buyerId) => {
  try {
    const response = await apiClient.get(`/api/transactions/buyer/${buyerId}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.response?.data?.message || error.message || 'Failed to fetch transactions',
      status: error.response?.status || 500,
    };
  }
};

/**
 * Get transactions by seller ID
 * @param {string} sellerId - Seller ID
 * @returns {Promise<Array>} Array of transaction objects
 */
export const getTransactionsBySeller = async (sellerId) => {
  try {
    const response = await apiClient.get(`/api/transactions/seller/${sellerId}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.response?.data?.message || error.message || 'Failed to fetch transactions',
      status: error.response?.status || 500,
    };
  }
};

