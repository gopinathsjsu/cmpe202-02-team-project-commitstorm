import apiClient from './apiClient.js';

/**
 * fetch S3Urls with image
 * @param {Object} data - formated filename and contentType
 * @returns {Promise<Array>} 
 */
export const getS3Url = async (data, batch=false) =>{
  try{
    let url = '/api/images/presigned-url'
    if (batch){ url += '/batch'}
    const response = await apiClient.post('/api/images/presigned-url', data);
    return response.data;
  } catch (error) {
    throw {
      message: error.message || 'Failed to create S3 Urls',
      status: error.status || 500,
    };
  }
}