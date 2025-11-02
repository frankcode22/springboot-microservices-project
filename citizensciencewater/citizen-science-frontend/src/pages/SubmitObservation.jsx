import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Send, MapPin, Droplet, User, Rss, Cloud, Sun, Lock, Loader2 } from 'lucide-react'; 

const API_SUBMIT_URL = 'http://localhost:8090/gateway/observations'; 
const API_ME_URL = 'http://localhost:8083/api/auth/me'; // Endpoint to get current user info

export default function SubmitObservation() {
  const [formData, setFormData] = useState({
    citizenId: '', // Will be auto-populated upon successful /me fetch
    postcode: '',         
    temperature: '',
    ph: '',               
    alkalinity: '',       
    turbidity: '',        
    observations: '',     
    imagePaths: '',       
  });
  const [status, setStatus] = useState({ message: null, type: null });
  const [loading, setLoading] = useState(false);
  const [isAuth, setIsAuth] = useState(false); // Tracks if we successfully authenticated via /me
  const [isReady, setIsReady] = useState(false); // Tracks if the initial auth check is complete
  
  // --- Authentication and User Info Fetch (using /me endpoint) ---
  useEffect(() => {
    const fetchCitizenId = async () => {
        setIsReady(false); // Set to false while checking auth
        const token = localStorage.getItem('accessToken');
        
        if (!token) {
            setStatus({ 
                message: "Authentication failed: No access token found. Please log in.", 
                type: 'error' 
            });
            setIsAuth(false);
            setIsReady(true);
            return;
        }

        try {
            // Fetch user data using the token in the Authorization header
            const response = await axios.get(API_ME_URL, {
                headers: {
                    // Send the token in the required format
                    'Authorization': `Bearer ${token}` 
                }
            });

            // Assuming the /me endpoint returns the citizenId directly in the body
            const citizenId = response.data.citizenId;

            if (citizenId) {
                setFormData(prev => ({
                    ...prev,
                    citizenId: citizenId
                }));
                setIsAuth(true);
                setStatus({ message: null, type: null }); // Clear previous error messages
            } else {
                setStatus({ 
                    message: "Authentication error: Citizen ID not found in user data.", 
                    type: 'error' 
                });
                setIsAuth(false);
            }

        } catch (err) {
            console.error("Error fetching user data via /me:", err);
            const errorMsg = err.response?.data?.error || "Token invalid or expired. Please sign in again.";
            setStatus({ 
                message: `Authentication failed: ${errorMsg}`, 
                type: 'error' 
            });
            setIsAuth(false);
        } finally {
            setIsReady(true);
        }
    };

    fetchCitizenId();
  }, []); // Run only once on component mount

  // --- Form Handlers ---

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setStatus({ message: null, type: null });

    // Final authentication and data validation checks
    if (!isReady || !isAuth || !formData.citizenId) {
        setLoading(false);
        return setStatus({ message: 'Authentication required. Please wait for ID check or log in.', type: 'error' });
    }
    if (!formData.postcode || !formData.ph || !formData.alkalinity || !formData.turbidity) {
      setLoading(false);
      return setStatus({ message: 'Please fill out all required fields: Postcode, pH, Alkalinity, and Turbidity.', type: 'error' });
    }
    
    try {
      // 1. Prepare data 
      const submissionData = {
          citizenId: formData.citizenId,
          postcode: formData.postcode,
          temperature: parseFloat(formData.temperature) || 0,
          ph: parseFloat(formData.ph),
          alkalinity: parseFloat(formData.alkalinity),
          turbidity: parseFloat(formData.turbidity),
          observations: formData.observations.split(',').map(s => s.trim()).filter(s => s),
          imagePaths: formData.imagePaths.split(',').map(s => s.trim()).filter(s => s),
      };

      // 2. Use AXIOS to send the data
      const token = localStorage.getItem('accessToken'); // Re-fetch token for submission if needed
      const response = await axios.post(API_SUBMIT_URL, submissionData, {
          // If the observation endpoint requires authentication, include the header
          headers: {
              'Authorization': `Bearer ${token}` 
          }
      });

      setStatus({ 
        message: `Observation successfully submitted! Response ID: ${response.data.id || 'Success'}`, 
        type: 'success' 
      });
      // Clear the form (excluding the citizenId)
      setFormData(prev => ({ 
          ...prev, 
          postcode: '', 
          temperature: '', 
          ph: '', 
          alkalinity: '', 
          turbidity: '', 
          observations: '', 
          imagePaths: '' 
      }));
      
    } catch (err) {
      const errorMsg = err.response?.data?.message || err.response?.data?.error || `Submission failed: ${err.message}. Please check your server status.`;
      setStatus({ message: errorMsg, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  if (!isReady) {
      return (
        <div className="flex items-center justify-center min-h-screen bg-gray-100">
            <Loader2 className="w-8 h-8 mr-2 animate-spin text-blue-500" />
            <p className="text-xl text-gray-700 font-medium">Authenticating user...</p>
        </div>
      );
  }

  return (
    <div className="max-w-4xl mx-auto p-4 sm:p-6 lg:p-8">
      <h1 className="text-3xl font-extrabold text-blue-800 mb-2 border-b pb-2">🌊 Water Observation Report</h1>
      <p className="text-gray-600 mb-8">Submit water quality data. Fields marked with <span className="font-bold text-red-500">*</span> are required.</p>

      {status.message && (
        <div className={`p-4 mb-6 rounded-lg font-medium shadow-sm transition-all duration-300 ${
          status.type === 'error' ? 'bg-red-100 text-red-700 border border-red-300' : 'bg-green-100 text-green-700 border border-green-300'
        }`}>
          {status.message}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6 bg-white p-8 rounded-xl shadow-2xl">
        {/* Citizen ID (Auto-populated and Disabled) and Postcode */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="relative">
            <label htmlFor="citizenId" className="block text-sm font-bold text-gray-700 flex items-center">
                {isAuth ? <Lock className="w-4 h-4 mr-1 text-blue-500" /> : <User className="w-4 h-4 mr-1 text-red-500" />} 
                Citizen ID ({isAuth ? 'Authenticated' : 'Missing'})
            </label>
            <input 
              type="text" 
              name="citizenId" 
              id="citizenId" 
              value={formData.citizenId || 'Please Log In'} 
              readOnly 
              disabled 
              className={`mt-1 block w-full border-gray-300 rounded-md shadow-inner p-3 text-gray-700 ${isAuth ? 'bg-gray-100' : 'bg-red-50 italic text-red-700'}`}
              title="This field is populated automatically after successful token validation via the /me endpoint."
            />
          </div>

          <div>
            <label htmlFor="postcode" className="block text-sm font-medium text-gray-700 flex items-center"><MapPin className="w-4 h-4 mr-1" /> Postcode <span className="font-bold text-red-500">*</span></label>
            <input type="text" name="postcode" id="postcode" value={formData.postcode} onChange={handleChange} required className="mt-1 block w-full border-gray-300 rounded-md shadow-sm p-3 focus:ring-blue-500 focus:border-blue-500" placeholder="e.g., NE1 8ST" />
          </div>
        </div>
        
        {/* Core Water Quality Metrics: pH and Temperature */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label htmlFor="ph" className="block text-sm font-medium text-gray-700 flex items-center"><Droplet className="w-4 h-4 mr-1" /> pH Level <span className="font-bold text-red-500">*</span> (0-14)</label>
            <input type="number" name="ph" id="ph" value={formData.ph} onChange={handleChange} required min="0" max="14" step="0.1" className="mt-1 block w-full border-gray-300 rounded-md shadow-sm p-3 focus:ring-blue-500 focus:border-blue-500" placeholder="e.g., 7.2" />
          </div>
          
          <div>
            <label htmlFor="temperature" className="block text-sm font-medium text-gray-700 flex items-center">🌡️ Temperature (°C)</label>
            <input type="number" name="temperature" id="temperature" value={formData.temperature} onChange={handleChange} step="0.1" className="mt-1 block w-full border-gray-300 rounded-md shadow-sm p-3 focus:ring-blue-500 focus:border-blue-500" placeholder="e.g., 15.5" />
          </div>
        </div>

        {/* Extended Water Quality Metrics: Alkalinity and Turbidity */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label htmlFor="alkalinity" className="block text-sm font-medium text-gray-700 flex items-center"><Sun className="w-4 h-4 mr-1" /> Alkalinity <span className="font-bold text-red-500">*</span> (mg/L)</label>
            <input type="number" name="alkalinity" id="alkalinity" value={formData.alkalinity} onChange={handleChange} required step="0.1" className="mt-1 block w-full border-gray-300 rounded-md shadow-sm p-3 focus:ring-blue-500 focus:border-blue-500" placeholder="e.g., 120.0" />
          </div>
          
          <div>
            <label htmlFor="turbidity" className="block text-sm font-medium text-gray-700 flex items-center"><Cloud className="w-4 h-4 mr-1" /> Turbidity <span className="font-bold text-red-500">*</span> (NTU)</label>
            <input type="number" name="turbidity" id="turbidity" value={formData.turbidity} onChange={handleChange} required step="0.1" className="mt-1 block w-full border-gray-300 rounded-md shadow-sm p-3 focus:ring-blue-500 focus:border-blue-500" placeholder="e.g., 5.0" />
          </div>
        </div>

        {/* Observations and Images (Comma-Separated) */}
        <div>
          <label htmlFor="observations" className="block text-sm font-medium text-gray-700 flex items-center"><Rss className="w-4 h-4 mr-1" /> Visual Observations (Comma-separated)</label>
          <textarea name="observations" id="observations" rows="2" value={formData.observations} onChange={handleChange} className="mt-1 block w-full border-gray-300 rounded-md shadow-sm p-3 focus:ring-blue-500 focus:border-blue-500" placeholder="e.g., Clear, No Odour, Low Flow" />
        </div>
        
        <div>
          <label htmlFor="imagePaths" className="block text-sm font-medium text-gray-700">Image Paths (Comma-separated)</label>
          <input type="text" name="imagePaths" id="imagePaths" value={formData.imagePaths} onChange={handleChange} className="mt-1 block w-full border-gray-300 rounded-md shadow-sm p-3 focus:ring-blue-500 focus:border-blue-500" placeholder="e.g., /path/to/image1.jpg, /path/to/image2.jpg" />
        </div>

        <div>
          <button type="submit" disabled={loading || !isAuth} className={`w-full flex items-center justify-center py-3 px-4 border border-transparent rounded-md shadow-lg text-lg font-semibold text-white transition-all duration-300 transform hover:scale-[1.01] ${loading || !isAuth ? 'bg-red-400 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-4 focus:ring-blue-300'}`}>
            <Send className="w-5 h-5 mr-2" />
            {loading ? 'Submitting...' : !isAuth ? 'Log In to Submit' : 'Submit Observation'}
          </button>
        </div>
      </form>
    </div>
  );
}
