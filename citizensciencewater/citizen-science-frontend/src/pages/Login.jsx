// src/pages/Login.jsx

import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

// Base URL for the Spring Boot Auth Microservice, accessible via the API Gateway
const API_BASE_URL = 'http://localhost:8083/api/auth'; 

function Login() {
  const navigate = useNavigate();
  // State to toggle between 'login' and 'register' view
  const [isRegisterMode, setIsRegisterMode] = useState(false); 
  
  // State for form inputs (shared structure for username and password)
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '', // Only used for registration
    fullName: '', // Only used for registration
    confirmPassword: '', // Only used for registration
  });
  
  const [error, setError] = useState(null);
  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(false);

  // --- Utility Functions ---

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError(null); // Clear errors on input change
  };

  const clearFormAndMessages = () => {
    setFormData({ username: '', password: '', email: '', confirmPassword: '' });
    setError(null);
    setMessage(null);
  };
  
  // Saves the JWT and user info (required for subsequent API calls)
  const handleAuthSuccess = (response) => {
    // Store tokens/user data securely (e.g., in localStorage or a state management solution)
    localStorage.setItem('accessToken', response.token);
    localStorage.setItem('user', JSON.stringify({ username: response.username, roles: response.roles }));
    
    setLoading(false);
    clearFormAndMessages();

    // Navigate to the secured dashboard hub
    navigate('/dashboard-hub');
  };

  // --- Form Handlers ---

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setMessage(null);
    
    try {
      const response = await axios.post(`${API_BASE_URL}/login`, {
        username: formData.username,
        password: formData.password
      });

      handleAuthSuccess(response.data);
      
    } catch (err) {
      setLoading(false);
      // The controller returns 401 with a specific error for invalid credentials
      const errorMessage = err.response?.data?.error || 'Login failed. Check your credentials.';
      setError(errorMessage);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setMessage(null);

    if (formData.password !== formData.confirmPassword) {
      setLoading(false);
      return setError('Passwords do not match.');
    }
    
    try {
      const response = await axios.post(`${API_BASE_URL}/register`, {
        username: formData.username,
        password: formData.password,
        email: formData.email,
        fullName: formData.fullName,
        // You may need to include other required fields from your RegisterRequest DTO here (e.g., postcode if required for citizen ID)
      });
      
      // Since registration is successful, we can auto-login or just show a message
      // The Spring controller returns AuthResponse on success, so we'll treat it as successful sign-in
      setMessage('Registration successful! Logging you in...');
      handleAuthSuccess(response.data);
      
    } catch (err) {
      setLoading(false);
      // Handle 400 Bad Request (e.g., username/email already taken, validation errors)
      const errorMessage = err.response?.data?.error || 'Registration failed. Try a different username or email.';
      setError(errorMessage);
    }
  };

  // --- Render Function ---

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 p-4">
      <div className="w-full max-w-md bg-white rounded-xl shadow-2xl overflow-hidden">
        
        {/* Tab Navigation */}
        <div className="flex border-b border-gray-200">
          <button
            onClick={() => { setIsRegisterMode(false); clearFormAndMessages(); }}
            className={`flex-1 py-4 text-center font-semibold text-lg transition-colors duration-300 ${
              !isRegisterMode
                ? 'bg-blue-600 text-white'
                : 'text-gray-600 hover:bg-gray-50'
            }`}
          >
            Sign In
          </button>
          <button
            onClick={() => { setIsRegisterMode(true); clearFormAndMessages(); }}
            className={`flex-1 py-4 text-center font-semibold text-lg transition-colors duration-300 ${
              isRegisterMode
                ? 'bg-blue-600 text-white'
                : 'text-gray-600 hover:bg-gray-50'
            }`}
          >
            Register
          </button>
        </div>

        {/* Form Content */}
        <div className="p-8">
          <h2 className="text-3xl font-extrabold text-gray-900 text-center mb-6">
            {isRegisterMode ? 'New Citizen Account' : 'Citizen Sign In'}
          </h2>

          {(error || message) && (
            <div className={`p-3 mb-4 rounded-md text-sm font-medium ${
              error ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'
            }`}>
              {error || message}
            </div>
          )}

          <form onSubmit={isRegisterMode ? handleRegister : handleLogin} className="space-y-6">
            <div>
              <label htmlFor="username" className="block text-sm font-medium text-gray-700">
                Username
              </label>
              <input
                id="username"
                name="username"
                type="text"
                required
                value={formData.username}
                onChange={handleChange}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                placeholder="Enter your unique username"
              />
            </div>

            {isRegisterMode && (
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                  Email Address
                </label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  required
                  value={formData.email}
                  onChange={handleChange}
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  placeholder="name@example.com"
                />
              </div>
            )}

 {isRegisterMode && (
             <div>
              <label htmlFor="username" className="block text-sm font-medium text-gray-700">
                Full Name
              </label>
              <input
                id="fullName"
                name="fullName"
                type="text"
                required
                value={formData.fullName}
                onChange={handleChange}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                placeholder="Enter your Full Name"
              />
            </div>

              )}

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                Password
              </label>
              <input
                id="password"
                name="password"
                type="password"
                required
                value={formData.password}
                onChange={handleChange}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                placeholder="••••••••"
              />
            </div>

            {isRegisterMode && (
              <div>
                <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">
                  Confirm Password
                </label>
                <input
                  id="confirmPassword"
                  name="confirmPassword"
                  type="password"
                  required
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  placeholder="••••••••"
                />
              </div>
            )}

            <div>
              <button
                type="submit"
                disabled={loading}
                className={`w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-lg font-medium text-white transition-colors ${
                  loading
                    ? 'bg-blue-400 cursor-not-allowed'
                    : 'bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500'
                }`}
              >
                {loading 
                  ? (isRegisterMode ? 'Registering...' : 'Signing In...')
                  : (isRegisterMode ? 'Register Account' : 'Sign In')
                }
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default Login;