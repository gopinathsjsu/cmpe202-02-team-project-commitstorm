// Example:
// <Signup onAuthSuccess={({ token, ...user }) => { /* navigate('/dashboard') */ }} />

import { useState } from 'react';
import { register } from '../../services/authService.js';
import { setAuthToken } from '../../services/apiClient.js';
import { required, isEmail, minLength, match } from '../../lib/validators.js';
import AuthModal from './AuthModal.jsx';

const Signup = ({ onAuthSuccess, onClose }) => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
    
    // Clear field error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};
    
    // Validate name
    const nameError = required(formData.name);
    if (nameError) {
      newErrors.name = nameError;
    }
    
    // Validate email
    const emailError = required(formData.email) || isEmail(formData.email);
    if (emailError) {
      newErrors.email = emailError;
    }
    
    // Validate password
    const passwordError = required(formData.password) || minLength(8)(formData.password);
    if (passwordError) {
      newErrors.password = passwordError;
    }
    
    // Validate confirm password
    const confirmPasswordError = required(formData.confirmPassword) || 
      match(formData.password, formData.confirmPassword);
    if (confirmPasswordError) {
      newErrors.confirmPassword = confirmPasswordError;
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    setIsLoading(true);
    setErrorMessage('');
    
    try {
      const response = await register({
        name: formData.name,
        email: formData.email,
        password: formData.password,
      });
      
      // Store auth data
      localStorage.setItem('auth.token', response.token);
      localStorage.setItem('auth.user', JSON.stringify({
        id: response.id,
        name: response.name,
        email: response.email,
        role: response.role,
        status: response.status,
      }));
      
      // Set auth token for future requests
      setAuthToken(response.token);
      
      // Call success callback if provided
      if (onAuthSuccess) {
        onAuthSuccess({
          token: response.token,
          id: response.id,
          name: response.name,
          email: response.email,
          role: response.role,
          status: response.status,
        });
      }
    } catch (error) {
      setErrorMessage(error.message || 'Registration failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthModal title="Sign Up" onClose={onClose}>
      {errorMessage && (
        <div 
          className="rounded-xl bg-red-50 text-red-700 text-sm p-3"
          role="alert"
        >
          {errorMessage}
        </div>
      )}
      
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label 
            htmlFor="name" 
            className="block text-sm font-medium text-gray-700"
          >
            Full Name
          </label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            className={`w-full h-12 rounded-xl border px-3 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 text-gray-900 ${
              errors.name ? 'border-red-500' : 'border-gray-300'
            }`}
            aria-invalid={errors.name ? 'true' : 'false'}
            disabled={isLoading}
            placeholder="Enter your full name"
          />
          {errors.name && (
            <p className="mt-1 text-xs text-red-600">{errors.name}</p>
          )}
        </div>
        
        <div>
          <label 
            htmlFor="email" 
            className="block text-sm font-medium text-gray-700"
          >
            Email
          </label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            className={`w-full h-12 rounded-xl border px-3 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 text-gray-900 ${
              errors.email ? 'border-red-500' : 'border-gray-300'
            }`}
            aria-invalid={errors.email ? 'true' : 'false'}
            disabled={isLoading}
            placeholder="Enter your email"
          />
          {errors.email && (
            <p className="mt-1 text-xs text-red-600">{errors.email}</p>
          )}
        </div>
        
        <div>
          <label 
            htmlFor="password" 
            className="block text-sm font-medium text-gray-700"
          >
            Password
          </label>
          <input
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            className={`w-full h-12 rounded-xl border px-3 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 text-gray-900 ${
              errors.password ? 'border-red-500' : 'border-gray-300'
            }`}
            aria-invalid={errors.password ? 'true' : 'false'}
            disabled={isLoading}
            placeholder="Create a password (min. 8 characters)"
          />
          {errors.password && (
            <p className="mt-1 text-xs text-red-600">{errors.password}</p>
          )}
        </div>
        
        <div>
          <label 
            htmlFor="confirmPassword" 
            className="block text-sm font-medium text-gray-700"
          >
            Confirm Password
          </label>
          <input
            type="password"
            id="confirmPassword"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleChange}
            className={`w-full h-12 rounded-xl border px-3 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 text-gray-900 ${
              errors.confirmPassword ? 'border-red-500' : 'border-gray-300'
            }`}
            aria-invalid={errors.confirmPassword ? 'true' : 'false'}
            disabled={isLoading}
            placeholder="Confirm your password"
          />
          {errors.confirmPassword && (
            <p className="mt-1 text-xs text-red-600">{errors.confirmPassword}</p>
          )}
        </div>
        
        <button
          type="submit"
          disabled={isLoading}
          className="w-full h-12 rounded-xl font-semibold bg-indigo-600 text-white hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none focus:ring-2 focus:ring-indigo-200"
        >
          {isLoading ? (
            <div className="flex items-center justify-center">
              <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Creating Account...
            </div>
          ) : (
            'Sign Up'
          )}
        </button>
      </form>
      
      <div className="text-center text-sm text-gray-600">
        Already have an account?{' '}
        <button
          type="button"
          onClick={() => {
            // This would be handled by the parent component to switch to login
            console.log('Switch to login');
          }}
          className="text-indigo-600 hover:text-indigo-700 font-medium"
        >
          Log in
        </button>
      </div>
    </AuthModal>
  );
};

export default Signup;
