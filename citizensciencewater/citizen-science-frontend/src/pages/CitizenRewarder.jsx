import React, { useState } from 'react';
import axios from 'axios';
import { User, DollarSign, Award, Send, Search, AlertTriangle, CheckCircle, Loader2 } from 'lucide-react'; 

// API Base URL for the rewards service
const API_REWARDS_BASE_URL = 'http://localhost:8082/api/rewards'; 


const API_GATEWAY_URL = 'http://localhost:8090/gateway/rewards'; 

export default function CitizenRewarder() {
  const [targetId, setTargetId] = useState('');
  const [rewardPoints, setRewardPoints] = useState(1);
  const [rewardBadge, setRewardBadge] = useState(''); // Can be 'Bronze', 'Silver', 'Gold'
  
  const [citizenStatus, setCitizenStatus] = useState(null);
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState({ message: null, type: null });

  const badgeOptions = [
    { value: '', label: 'No Badge Change (Points Only)' },
    { value: 'Bronze', label: 'Bronze Badge' },
    { value: 'Silver', label: 'Silver Badge' },
    { value: 'Gold', label: 'Gold Badge' },
  ];

  // Function to look up the citizen's current status
  const handleLookup = async () => {
    if (!targetId.trim()) {
      setStatus({ message: 'Please enter a Citizen ID to look up.', type: 'error' });
      return;
    }
    
    setLoading(true);
    setStatus({ message: null, type: null });
    setCitizenStatus(null); // Clear previous status
    
    try {
    //   const response = await axios.get(`${API_REWARDS_BASE_URL}/citizen/${targetId}`);


      const response = await axios.get(`${API_GATEWAY_URL}/citizen/${targetId}`);


      
      
      setCitizenStatus(response.data);
      setStatus({ message: `Status loaded for ${targetId}. Ready to reward.`, type: 'success' });

    } catch (err) {
      console.error("Lookup Error:", err.response ? err.response.data : err.message);
      const errorMsg = err.response && err.response.status === 404
        ? `Citizen ID ${targetId} not found in rewards system.`
        : err.response?.data?.error || `Failed to look up citizen. Check API status.`;
      
      setStatus({ message: errorMsg, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  // Function to submit the reward transaction
  const handleReward = async (e) => {
    e.preventDefault();

    if (!citizenStatus) {
        setStatus({ message: 'Please look up the citizen status first.', type: 'error' });
        return;
    }
    if (rewardPoints < 1) {
        setStatus({ message: 'Points must be 1 or greater.', type: 'error' });
        return;
    }

    setLoading(true);
    setStatus({ message: null, type: null });

    try {
        const payload = { 
            points: rewardPoints,
            // Only include the badge field if a specific badge is selected
            ...(rewardBadge && { badge: rewardBadge })
        };

        // const url = `${API_REWARDS_BASE_URL}/citizen/${targetId}/reward`;

        const url = `${API_GATEWAY_URL}/citizen/${targetId}/reward`;
        
        const response = await axios.post(url, payload);

        setStatus({ 
            message: `Successfully rewarded ${targetId}: ${rewardPoints} points granted. New Total Points: ${response.data.totalPoints}.`, 
            type: 'success' 
        });
        // Immediately refresh the citizen status after a successful reward
        await handleLookup(); 

    } catch (err) {
        console.error("Reward Submission Error:", err.response ? err.response.data : err.message);
        const errorMsg = err.response?.data?.error || `Reward submission failed: ${err.message}.`;
        setStatus({ message: errorMsg, type: 'error' });
    } finally {
        setLoading(false);
    }
  };

  const StatusIcon = status.type === 'success' ? CheckCircle : AlertTriangle;

  return (
    <div className="max-w-4xl mx-auto p-4 sm:p-6 lg:p-8 bg-gray-50 rounded-xl shadow-2xl">
      <h1 className="text-3xl font-extrabold text-indigo-700 mb-6 border-b pb-2 flex items-center">
        <Award className="w-8 h-8 mr-2" /> Citizen Reward Transaction Tool
      </h1>
      <p className="text-gray-600 mb-6">Use this interface to manually verify and grant rewards to citizen scientists.</p>

      {/* Status Message */}
      {status.message && (
        <div className={`p-4 mb-6 rounded-lg font-medium shadow-sm transition-all duration-300 flex items-center ${
          status.type === 'error' ? 'bg-red-100 text-red-700 border border-red-300' : 'bg-green-100 text-green-700 border border-green-300'
        }`}>
          <StatusIcon className="w-5 h-5 mr-2" />
          {status.message}
        </div>
      )}

      {/* 1. Citizen Lookup Section */}
      <div className="bg-white p-6 rounded-lg shadow-md mb-8">
        <h2 className="text-xl font-semibold text-gray-800 mb-4 flex items-center"><User className="w-5 h-5 mr-2 text-blue-500" /> Target Citizen ID</h2>
        <div className="flex flex-col sm:flex-row gap-4">
          <input 
            type="text" 
            placeholder="Enter Citizen ID (e.g., CS-POSTMAN-001)"
            value={targetId}
            onChange={(e) => setTargetId(e.target.value.toUpperCase())}
            className="flex-grow p-3 border border-gray-300 rounded-lg shadow-inner focus:ring-blue-500 focus:border-blue-500"
            disabled={loading}
          />
          <button 
            onClick={handleLookup} 
            disabled={loading || !targetId.trim()}
            className="w-full sm:w-auto px-6 py-3 bg-blue-600 text-white font-bold rounded-lg shadow-lg hover:bg-blue-700 transition disabled:bg-blue-400 disabled:cursor-not-allowed flex items-center justify-center"
          >
            {loading ? <Loader2 className="w-5 h-5 mr-2 animate-spin" /> : <Search className="w-5 h-5 mr-2" />}
            Lookup Status
          </button>
        </div>
      </div>
      
      {/* 2. Citizen Status Display */}
      {citizenStatus && (
        <div className="bg-indigo-50 p-6 rounded-lg shadow-md border-l-4 border-indigo-500 mb-8">
            <h2 className="text-xl font-semibold text-indigo-800 mb-3">Current Status for: <span className="font-extrabold">{citizenStatus.citizenId}</span></h2>
            <div className="grid grid-cols-2 gap-4 text-gray-700">
                <p><strong>Total Points:</strong> <span className="text-2xl font-bold text-indigo-600">{citizenStatus.totalPoints}</span></p>
                <p><strong>Valid Observations:</strong> <span className="text-xl font-bold text-indigo-600">{citizenStatus.validObservations}</span></p>
                <p><strong>Current Badge:</strong> <span className="text-xl font-bold text-indigo-600">{citizenStatus.currentBadge || 'None'}</span></p>
                <p><strong>Badges Achieved:</strong> <span className="font-medium">{citizenStatus.badges}</span></p>
            </div>
        </div>
      )}

      {/* 3. Reward Submission Form */}
      {citizenStatus && (
        <form onSubmit={handleReward} className="bg-white p-6 rounded-lg shadow-lg">
            <h2 className="text-xl font-semibold text-gray-800 mb-4 flex items-center"><DollarSign className="w-5 h-5 mr-2 text-green-500" /> Grant Reward</h2>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                {/* Points Input */}
                <div>
                    <label htmlFor="rewardPoints" className="block text-sm font-medium text-gray-700">Points to Grant (min 1)</label>
                    <input 
                        type="number" 
                        id="rewardPoints"
                        value={rewardPoints}
                        onChange={(e) => setRewardPoints(Math.max(1, parseInt(e.target.value) || 1))}
                        required
                        min="1"
                        className="mt-1 block w-full p-3 border border-gray-300 rounded-lg shadow-sm focus:ring-green-500 focus:border-green-500"
                    />
                </div>
                
                {/* Badge Selection */}
                <div>
                    <label htmlFor="rewardBadge" className="block text-sm font-medium text-gray-700">Optional Badge to Grant</label>
                    <select
                        id="rewardBadge"
                        value={rewardBadge}
                        onChange={(e) => setRewardBadge(e.target.value)}
                        className="mt-1 block w-full p-3 border border-gray-300 rounded-lg shadow-sm focus:ring-green-500 focus:border-green-500 bg-white"
                    >
                        {badgeOptions.map(option => (
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            <button 
                type="submit" 
                disabled={loading || !citizenStatus}
                className={`w-full flex items-center justify-center py-3 px-4 rounded-md shadow-lg text-lg font-semibold text-white transition-all duration-300 transform hover:scale-[1.005] ${loading ? 'bg-gray-400 cursor-not-allowed' : 'bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-4 focus:ring-green-300'}`}
            >
                {loading ? <Loader2 className="w-5 h-5 mr-2 animate-spin" /> : <Send className="w-5 h-5 mr-2" />}
                Grant {rewardPoints} Points to {targetId}
            </button>
        </form>
      )}

    </div>
  );
}
