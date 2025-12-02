import apiClient from './apiClient.js';

/**
 * Get all listings from the API
 * @param {string} searchQuery - Optional search query to filter listings
 * @returns {Promise<Array>} Array of listing objects
 */
export const getListings = async (searchQuery = '') => {
  try {
    const params = {};
    if (searchQuery && searchQuery.trim()) {
      params.search = searchQuery.trim();
    }
    const response = await apiClient.get('/api/listings', { params });
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch listings',
      status: error.status || 500,
    };
  }
};

/**
 * Get a single listing by ID
 * @param {string} listingId - UUID of the listing
 * @returns {Promise<Object>} Listing object
 */
export const getListingById = async (listingId) => {
  try {
    const response = await apiClient.get(`/api/listings/${listingId}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch listing',
      status: error.status || 500,
    };
  }
};
