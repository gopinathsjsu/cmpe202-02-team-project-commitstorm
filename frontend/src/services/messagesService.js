import apiClient from './apiClient.js';

/**
 * Send a message
 * @param {Object} messageData - { listingId, toUserId, content }
 * @returns {Promise<Object>} Message object
 */
export const sendMessage = async (messageData) => {
  try {
    const response = await apiClient.post('/api/messages', messageData);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to send message',
      status: error.status || 500,
    };
  }
};

/**
 * Get message by ID
 * @param {string} messageId - Message ID
 * @returns {Promise<Object>} Message object
 */
export const getMessageById = async (messageId) => {
  try {
    const response = await apiClient.get(`/api/messages/${messageId}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch message',
      status: error.status || 500,
    };
  }
};

/**
 * Get messages for a listing
 * @param {string} listingId - Listing ID
 * @returns {Promise<Array>} Array of messages
 */
export const getMessagesByListing = async (listingId) => {
  try {
    const response = await apiClient.get(`/api/messages/listing/${listingId}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch messages',
      status: error.status || 500,
    };
  }
};

/**
 * Get conversation between two users
 * @param {string} userId1 - First user ID
 * @param {string} userId2 - Second user ID
 * @returns {Promise<Array>} Array of messages
 */
export const getConversation = async (userId1, userId2) => {
  try {
    const response = await apiClient.get(`/api/messages/conversation/${userId1}/${userId2}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch conversation',
      status: error.status || 500,
    };
  }
};

/**
 * Get conversation for a specific listing
 * @param {string} listingId - Listing ID
 * @param {string} userId1 - First user ID
 * @param {string} userId2 - Second user ID
 * @param {number} page - Page number (optional)
 * @param {number} size - Page size (optional)
 * @returns {Promise<Object>} Paginated messages
 */
export const getListingConversation = async (listingId, userId1, userId2, page = 0, size = 20) => {
  try {
    const url = page !== undefined 
      ? `/api/messages/conversation/listing/${listingId}/${userId1}/${userId2}/page?page=${page}&size=${size}`
      : `/api/messages/conversation/listing/${listingId}/${userId1}/${userId2}`;
    const response = await apiClient.get(url);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch conversation',
      status: error.status || 500,
    };
  }
};

/**
 * Get messages sent by a user
 * @param {string} userId - User ID
 * @param {number} page - Page number
 * @param {number} size - Page size
 * @returns {Promise<Object>} Paginated messages
 */
export const getSentMessages = async (userId, page = 0, size = 20) => {
  try {
    const response = await apiClient.get(`/api/messages/sent/${userId}?page=${page}&size=${size}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch sent messages',
      status: error.status || 500,
    };
  }
};

/**
 * Get messages received by a user
 * @param {string} userId - User ID
 * @param {number} page - Page number
 * @param {number} size - Page size
 * @returns {Promise<Object>} Paginated messages
 */
export const getReceivedMessages = async (userId, page = 0, size = 20) => {
  try {
    const response = await apiClient.get(`/api/messages/received/${userId}?page=${page}&size=${size}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch received messages',
      status: error.status || 500,
    };
  }
};

/**
 * Get all messages for a user (sent + received)
 * @param {string} userId - User ID
 * @param {number} page - Page number
 * @param {number} size - Page size
 * @returns {Promise<Object>} Paginated messages
 */
export const getUserMessages = async (userId, page = 0, size = 20) => {
  try {
    const response = await apiClient.get(`/api/messages/user/${userId}?page=${page}&size=${size}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch messages',
      status: error.status || 500,
    };
  }
};

/**
 * Get conversation partners for a user
 * @param {string} userId - User ID
 * @returns {Promise<Array>} Array of user IDs
 */
export const getConversationPartners = async (userId) => {
  try {
    const response = await apiClient.get(`/api/messages/partners/${userId}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch conversation partners',
      status: error.status || 500,
    };
  }
};

/**
 * Get unread message count
 * @param {string} userId - User ID
 * @returns {Promise<number>} Unread count
 */
export const getUnreadCount = async (userId) => {
  try {
    const response = await apiClient.get(`/api/messages/unread/count/${userId}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch unread count',
      status: error.status || 500,
    };
  }
};

/**
 * Get unread messages
 * @param {string} userId - User ID
 * @returns {Promise<Array>} Array of unread messages
 */
export const getUnreadMessages = async (userId) => {
  try {
    const response = await apiClient.get(`/api/messages/unread/${userId}`);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to fetch unread messages',
      status: error.status || 500,
    };
  }
};

/**
 * Mark a message as read
 * @param {string} messageId - Message ID
 * @returns {Promise<void>}
 */
export const markMessageAsRead = async (messageId) => {
  try {
    await apiClient.patch(`/api/messages/${messageId}/mark-read`);
  } catch (error) {
    throw {
      message: error.message || 'Failed to mark message as read',
      status: error.status || 500,
    };
  }
};

/**
 * Mark all messages as read for a user
 * @param {string} userId - User ID
 * @returns {Promise<void>}
 */
export const markAllMessagesAsRead = async (userId) => {
  try {
    await apiClient.patch(`/api/messages/mark-all-read/${userId}`);
  } catch (error) {
    throw {
      message: error.message || 'Failed to mark all messages as read',
      status: error.status || 500,
    };
  }
};

/**
 * Delete a message
 * @param {string} messageId - Message ID
 * @returns {Promise<void>}
 */
export const deleteMessage = async (messageId) => {
  try {
    await apiClient.delete(`/api/messages/${messageId}`);
  } catch (error) {
    throw {
      message: error.message || 'Failed to delete message',
      status: error.status || 500,
    };
  }
};

