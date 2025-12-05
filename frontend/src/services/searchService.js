import apiClient from './apiClient.js';

export const askChatbot = async (query) => {
  try {
    let data = {
      query: query
    }
    const response = await apiClient.post('/api/listings/chatbot-search', data);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to query chatbot',
      status: error.status || 500,
    };
  }
};