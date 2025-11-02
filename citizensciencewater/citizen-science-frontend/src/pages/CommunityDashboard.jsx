// src/pages/CommunityDashboard.jsx

import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { Database, TrendingUp, Users, AlertTriangle, List } from 'lucide-react'; // Added List icon

const API_BASE_URL = 'http://localhost:8090/gateway/observations'; 

// --- Component Definition: StatCard (Unchanged) ---
const StatCard = ({ title, value, icon: Icon, color }) => (
  <div className="bg-white p-5 rounded-lg shadow-md border-l-4" style={{ borderColor: color }}>
    <div className="flex items-center">
      <Icon className={`w-6 h-6 mr-3 text-gray-600`} />
      <div>
        <p className="text-sm font-medium text-gray-500">{title}</p>
        <p className="text-2xl font-bold text-gray-900">{value}</p>
      </div>
    </div>
  </div>
);

// --- Component Definition: CommunityDashboard ---
export default function CommunityDashboard() {
  const { authorityName } = useParams();
  const [dashboardData, setDashboardData] = useState(null); 
  // NEW STATE for the detailed observation list
  const [allObservations, setAllObservations] = useState([]); 
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDashboardSummary = async () => {
      try {
        const response = await axios.get(API_BASE_URL); 
        setDashboardData(response.data);
      } catch (err) {
        console.error("Summary API Fetch Error:", err);
        const errorMessage = err.response 
            ? `Status ${err.response.status}: ${err.response.data?.message || err.message}` 
            : err.message;
        setError(`Failed to load summary data. Error: ${errorMessage}.`);
      }
    };
    
    // NEW FUNCTION to fetch the detailed list of observations
    const fetchAllObservations = async () => {
        try {
            // ASSUMING a /list endpoint exists for the full data set
            const response = await axios.get(`${API_BASE_URL}`);
            setAllObservations(response.data); 
            console
        } catch (err) {
            console.error("Observations List API Fetch Error:", err);
            // We can continue with the dashboard summary even if this fails
            console.warn("Failed to load full observation list.");
        }
    };

    const fetchData = async () => {
        setLoading(true);
        setError(null);
        // Run both fetches in parallel
        await Promise.all([fetchDashboardSummary(), fetchAllObservations()]);
        setLoading(false);
    };

    fetchData();
  }, [authorityName]);

  // Error and Loading States
  if (loading) {
    return <div className="text-center p-10 text-xl text-blue-600">Loading Dashboard for **{authorityName}**...</div>;
  }

  if (error) {
    return <div className="bg-red-100 text-red-700 p-4 rounded-lg"><AlertTriangle className="inline w-5 h-5 mr-2" /> Error: {error}</div>;
  }

  if (!dashboardData) {
      return <div className="text-center p-10 text-xl text-gray-500">No dashboard summary data available.</div>;
  }
  
  // Destructure data
  const { 
      totalObservations, 
      averagePH, 
      leaderboard 
  } = dashboardData;

  const highestRankedCitizenId = (leaderboard && leaderboard.length > 0) 
    ? leaderboard[0].citizenId 
    : 'N/A';

  // Helper function to format the timestamp
  const formatTimestamp = (timestamp) => {
    return new Date(timestamp).toLocaleString();
  };

  // --- Render Dashboard Content ---
  return (
    <div>
      <h1 className="text-3xl font-bold text-gray-900 mb-2">💧 **{authorityName}** Community Dashboard</h1>
      <p className="text-gray-600 mb-8">Detailed water quality observations and citizen ranking.</p>

      {/* 1. Summary Statistics */}
      <h2 className="text-2xl font-semibold text-gray-800 mb-4">Summary Statistics</h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
        <StatCard 
          title="Total Observations"
          value={totalObservations}
          icon={Database}
          color="#3b82f6"
        />
        <StatCard 
          title="Avg. Water pH"
          value={averagePH}
          icon={TrendingUp}
          color="#10b981"
        />
        <StatCard 
          title="Highest Ranked Citizen"
          value={highestRankedCitizenId}
          icon={Users}
          color="#f59e0b"
        />
      </div>
      
      {/* 3. Leaderboard - MOVED TO THE LEFT SIDE FOR SPACE */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1 bg-white p-6 rounded-xl shadow-lg h-fit"> 
            <h3 className="text-xl font-semibold mb-4 flex items-center"><Users className="w-5 h-5 mr-2" /> Top Citizens Leaderboard</h3>
            <ol className="space-y-3">
              {leaderboard?.map((citizen) => (
                <li key={citizen.rank} className={`flex justify-between items-center p-3 rounded-lg ${citizen.rank === 1 ? 'bg-yellow-50 border-2 border-yellow-500' : 'bg-gray-50'}`}>
                  <div className="flex items-center">
                    <span className={`w-6 h-6 flex items-center justify-center rounded-full text-white font-bold mr-3 ${citizen.rank === 1 ? 'bg-yellow-500' : 'bg-gray-400'}`}>{citizen.rank}</span>
                    <span className="font-medium">{citizen.citizenId}</span>
                  </div>
                  <span className="text-sm font-semibold text-blue-600">{citizen.points} Points</span>
                </li>
              ))}
              {(!leaderboard || leaderboard.length === 0) && (
                <li className="py-3 text-gray-500">Leaderboard data not available.</li>
              )}
            </ol>
        </div>
        
        {/* 2. All Observations Table - NEW SECTION */}
        <div className="lg:col-span-2 bg-white p-6 rounded-xl shadow-lg overflow-x-auto">
            <h3 className="text-xl font-semibold mb-4 flex items-center"><List className="w-5 h-5 mr-2" /> All Water Quality Observations ({allObservations.length})</h3>
            
            {allObservations.length === 0 ? (
                <div className="text-gray-500">No detailed observation data found.</div>
            ) : (
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                        <tr>
                            <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Citizen ID</th>
                            <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Postcode</th>
                            <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">pH</th>
                            <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Temp (°C)</th>
                            <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Timestamp</th>
                            <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Valid</th>
                        </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                        {allObservations.map((obs) => (
                            <tr key={obs.id}>
                                <td className="px-3 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{obs.citizenId}</td>
                                <td className="px-3 py-4 whitespace-nowrap text-sm text-gray-500">{obs.postcode}</td>
                                <td className="px-3 py-4 whitespace-nowrap text-sm text-gray-900 font-bold">{obs.ph.toFixed(1)}</td>
                                <td className="px-3 py-4 whitespace-nowrap text-sm text-gray-900">{obs.temperature.toFixed(1)}</td>
                                <td className="px-3 py-4 whitespace-nowrap text-sm text-gray-500">{formatTimestamp(obs.submissionTimestamp)}</td>
                                <td className="px-3 py-4 whitespace-nowrap text-sm">
                                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                        obs.valid ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                                    }`}>
                                        {obs.valid ? 'Yes' : 'No'}
                                    </span>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
      </div>
    </div>
  );
}