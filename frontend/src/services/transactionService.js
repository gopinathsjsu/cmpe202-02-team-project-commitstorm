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
 * Update transaction status
 * @param {string} transactionId - Transaction ID
 * @param {string} status - Status (COMPLETED, CANCELLED)
 * @returns {Promise<Object>} Updated transaction object
 */
export const updateTransactionStatus = async (transactionId, status) => {
  try {
    const response = await apiClient.patch(`/api/transactions/${transactionId}/status?status=${status}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.response?.data?.message || error.message || 'Failed to update transaction status',
      status: error.response?.status || 500,
    };
  }
};

/**
 * Confirm transaction (seller action) - updates transaction to COMPLETED
 * @param {string} transactionId - Transaction ID
 * @returns {Promise<Object>} Updated transaction object
 */
export const confirmTransaction = async (transactionId) => {
  return await updateTransactionStatus(transactionId, 'COMPLETED');
};

/**
 * Reject transaction (seller action) - updates transaction to CANCELLED
 * @param {string} transactionId - Transaction ID
 * @returns {Promise<Object>} Updated transaction object
 */
export const rejectTransaction = async (transactionId) => {
  return await updateTransactionStatus(transactionId, 'CANCELLED');
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

