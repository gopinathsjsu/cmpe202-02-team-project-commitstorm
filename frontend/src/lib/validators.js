/**
 * Validation utility functions
 * Returns null when valid, or a string message when invalid
 */

/**
 * Check if value is required (not empty)
 * @param {any} value - Value to validate
 * @returns {string|null} - Error message or null if valid
 */
export const required = (value) => {
  if (!value || (typeof value === 'string' && value.trim() === '')) {
    return 'This field is required';
  }
  return null;
};

/**
 * Check if value is a valid email format
 * @param {string} value - Email to validate
 * @returns {string|null} - Error message or null if valid
 */
export const isEmail = (value) => {
  if (!value) return null; // Let required handle empty values
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(value)) {
    return 'Please enter a valid email address';
  }
  return null;
};

/**
 * Check if value meets minimum length requirement
 * @param {number} minLength - Minimum required length
 * @returns {function} - Validator function
 */
export const minLength = (minLength) => (value) => {
  if (!value) return null; // Let required handle empty values
  
  if (value.length < minLength) {
    return `Must be at least ${minLength} characters long`;
  }
  return null;
};

/**
 * Check if two values match
 * @param {any} value1 - First value
 * @param {any} value2 - Second value to match against
 * @returns {string|null} - Error message or null if valid
 */
export const match = (value1, value2) => {
  if (value1 !== value2) {
    return 'Values do not match';
  }
  return null;
};
