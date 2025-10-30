// src/pages/SubmitObservation.jsx

import React, { useState } from 'react';
import axios from 'axios';
import { Send, MapPin, Droplet, User } from 'lucide-react';

const API_SUBMIT_URL = 'http://localhost:8080/api/v1/crowdsourced-data/submit'; 

export default function SubmitObservation() {
  const [formData, setFormData] = useState({
    citizenId: '',
    location: '',
    latitude: '',
    longitude: '',
    phLevel: '',
    temperature: '',
    notes: '',
  });
  const [status, setStatus] = useState({ message: null, type: null });
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setStatus({ message: null, type: null });

    // Simple validation for required fields
    if (!formData.citizenId || !formData.location || !formData.phLevel) {
        setLoading(false);
        return setStatus({ message: 'Please fill out all required fields.', type: 'error' });
    }
    
    try {
      // 1. Prepare data (ensure pH and temp are numbers)
      const submissionData = {
          ...formData,
          phLevel: parseFloat(formData.phLevel),
          temperature: parseFloat(formData.temperature),
          // Add timestamp and other context data here if needed
      };

      // 2. Use AXIOS to send the data to the Spring Boot microservice
      const response = await axios.post(API_SUBMIT_URL, submissionData);

      setStatus({ 
        message: `Observation successfully submitted! ID: ${response.data.observationId || 'N/A'}`, 
        type: 'success' 
      });
      setFormData({
        citizenId: '', location: '', latitude: '', longitude: '', phLevel: '', temperature: '', notes: '',
      });
      
    } catch (err) {
      const errorMsg = err.response?.data?.error || 'Submission failed. Check your microservice connection.';
      setStatus({ message: errorMsg, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-3xl font-bold text-gray-900 mb-2">🌊 Submit New Observation</h1>
      <p className="text-gray-600 mb-8">Record new water quality data to contribute to the Citizen Science project.</p>

      {status.message && (
        <div className={`p-4 mb-6 rounded-lg font-medium ${
          status.type === 'error' ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'
        }`}>
          {status.message}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6 bg-white p-8 rounded-xl shadow-lg">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            
          {/* Citizen ID */}
          <div>
            <label htmlFor="citizenId" className="block text-sm font-medium text-gray-700 flex items-center"><User className="w-4 h-4 mr-1" /> Citizen ID (Required)</label>
            <input
              type="text"
              name="citizenId"
              id="citizenId"
              value={formData.citizenId}
              onChange={handleChange}
              required
              className="mt-1 block w-full border-gray-300 rounded-md shadow-sm p-3 focus:ring-blue-500 focus:border-blue-500"
              placeholder="e.g., CS-4001"
            />
          </div>

          {/* Location Name */}
          <div>
            <label htmlFor="location" className="block text-sm font-medium text-gray-700 flex items-center"><MapPin className="w-4 h-4 mr-1" /> Location Name (Required)</label>
            <input
              type="text"
              name="location"
              id="location"
              value={formData.location}
              onChange={handleChange}
              required
              className="mt-1 block w-full border-gray-300 rounded-md shadow-sm p-3 focus:ring-blue-500 focus:border-blue-500"
              placeholder="e.g., River Thames, Bridge 5"
            />
          </div>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* pH Level */}
            <div>
              <label htmlFor="phLevel" className="block text-sm font-medium text-gray-700 flex items-center"><Droplet className="w-4 h-4 mr-1" /> pH Level (Required, 0-14)</label>
              <input
                type="number"
                name="phLevel"
                id="phLevel"
                value={formData.phLevel}
                onChange={handleChange}
                required
                min="0"
                max="14"
                step="0.1"
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm p-3 focus:ring-blue-500 focus:border-blue-500"
                placeholder="e.g., 7.5"
              />
            </div>
            
            {/* Temperature */}
            <div>
              <label htmlFor="temperature" className="block text-sm font-medium text-gray-700 flex items-center">🌡️ Temperature (°C)</label>
              <input
                type="number"
                name="temperature"
                id="temperature"
                value={formData.temperature}
                onChange={handleChange}
                step="0.1"
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm p-3 focus:ring-blue-500 focus:border-blue-500"
                placeholder="e.g., 18.2"
              />
            </div>
        </div>

        {/* Notes */}
        <div>
          <label htmlFor="notes" className="block text-sm font-medium text-gray-700">Additional Notes</label>
          <textarea
            name="notes"
            id="notes"
            rows="3"
            value={formData.notes}
            onChange={handleChange}
            className="mt-1 block w-full border-gray-300 rounded-md shadow-sm p-3 focus:ring-blue-500 focus:border-blue-500"
            placeholder="Describe the condition of the water or surroundings."
          />
        </div>

        <div>
          <button
            type="submit"
            disabled={loading}
            className={`w-full flex items-center justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-lg font-medium text-white transition-colors ${
              loading
                ? 'bg-gray-400 cursor-not-allowed'
                : 'bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500'
            }`}
          >
            <Send className="w-5 h-5 mr-2" />
            {loading ? 'Submitting...' : 'Submit Observation'}
          </button>
        </div>
      </form>
    </div>
  );
}