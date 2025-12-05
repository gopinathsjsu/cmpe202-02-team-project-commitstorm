import apiClient from './apiClient.js';

/**
 * Get all listings from the API
 * @param {string} searchQuery - Optional search query to filter listings
 * @returns {Promise<Array>} Array of listing objects
 */
export const getListings = async (searchQuery = '') => {
  try {
    let url = '/api/listings';
    if (searchQuery && searchQuery.trim()) {
      // Use search endpoint with searchTerm parameter
      url = `/api/listings/search?searchTerm=${encodeURIComponent(searchQuery.trim())}`;
    }
    const response = await apiClient.get(url);
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

/**
 * Post a new listing to the API
 * @param {Object} listingData  - Data for the new listing
 * @returns {Promise<Boolean>} Success status
 */
export const postListings = async (data) => {
  try {
    const response = await apiClient.post('/api/listings', data);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to create listing',
      status: error.status || 500,
    };
  }
};

/**
 * Get listings by seller ID
 * @param {string} sellerId - Seller user ID
 * @returns {Promise<Array>} Array of listing objects
 */
export const getListingsBySeller = async (sellerId) => {
  try {
    const response = await apiClient.get(`/api/listings/seller/${sellerId}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch listings by seller',
      status: error.status || 500,
    };
  }
};

/**
 * Get listings by category ID
 * @param {string} categoryId - Category ID
 * @returns {Promise<Array>} Array of listing objects
 */
export const getListingsByCategory = async (categoryId) => {
  try {
    const response = await apiClient.get(`/api/listings/category/${categoryId}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch listings by category',
      status: error.status || 500,
    };
  }
};

/**
 * Get listings by status
 * @param {string} status - Status (ACTIVE, SOLD, PENDING, DRAFT)
 * @returns {Promise<Array>} Array of listing objects
 */
export const getListingsByStatus = async (status) => {
  try {
    const response = await apiClient.get(`/api/listings/status/${status}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch listings by status',
      status: error.status || 500,
    };
  }
};

/**
 * Get listings by condition
 * @param {string} condition - Condition (NEW, LIKE_NEW, GOOD, FAIR, POOR)
 * @returns {Promise<Array>} Array of listing objects
 */
export const getListingsByCondition = async (condition) => {
  try {
    const response = await apiClient.get(`/api/listings/condition/${condition}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch listings by condition',
      status: error.status || 500,
    };
  }
};

/**
 * Get listings by price range
 * @param {number} minPrice - Minimum price
 * @param {number} maxPrice - Maximum price
 * @returns {Promise<Array>} Array of listing objects
 */
export const getListingsByPriceRange = async (minPrice, maxPrice) => {
  try {
    const params = {};
    if (minPrice !== undefined && minPrice !== null && minPrice !== '') {
      params.minPrice = minPrice;
    }
    if (maxPrice !== undefined && maxPrice !== null && maxPrice !== '') {
      params.maxPrice = maxPrice;
    }
    const response = await apiClient.get('/api/listings/price-range', { params });
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch listings by price range',
      status: error.status || 500,
    };
  }
};

/**
 * Update an existing listing
 * @param {string} listingId - UUID of the listing to update
 * @param {Object} listingData - Updated data for the listing
 * @returns {Promise<Object>} Updated listing object
 */
export const updateListing = async (listingId, listingData) => {
  try {
    const response = await apiClient.put(`/api/listings/${listingId}`, listingData);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to update listing',
      status: error.status || 500,
    };
  }
};

/**
 * Delete a listing
 * @param {string} listingId - UUID of the listing to delete
 * @returns {Promise<void>}
 */
export const deleteListing = async (listingId) => {
  try {
    await apiClient.delete(`/api/listings/${listingId}`);
  } catch (error) {
    throw {
      message: error.message || 'Failed to delete listing',
      status: error.status || 500,
    };
  }
};
