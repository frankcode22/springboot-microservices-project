// src/pages/CommunityDashboard.jsx

import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { Database, TrendingUp, Users, AlertTriangle } from 'lucide-react';

const API_BASE_URL = 'http://localhost:8080/api/v1'; // Base URL for the API Gateway

// --- Mock Data Structures (Replace with real API calls) ---

const mockData = {
  totalObservations: 452,
  averagePH: 7.2,
  recentObservations: [
    { id: 1, date: '2025-10-29', ph: 6.8, location: 'River Thames', status: 'Moderate' },
    { id: 2, date: '2025-10-28', ph: 7.5, location: 'Lake Serene', status: 'Good' },
    { id: 3, date: '2025-10-28', ph: 5.9, location: 'Canal Lock 3', status: 'Poor' },
    { id: 4, date: '2025-10-27', ph: 7.1, location: 'Pond near Park', status: 'Good' },
    { id: 5, date: '2025-10-27', ph: 8.0, location: 'Reservoir Outlet', status: 'Good' },
  ],
  leaderboard: [
    { rank: 1, citizenId: 'CS-4001', points: 1200 },
    { rank: 2, citizenId: 'CS-4005', points: 950 },
    { rank: 3, citizenId: 'CS-4002', points: 780 },
  ],
};

// --- Component Definition ---

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

export default function CommunityDashboard() {
  const { authorityName } = useParams();
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // 1. Fetch data from your specific microservice via API Gateway
    const fetchData = async () => {
      try {
        setLoading(true);
        setError(null);
        
        // This is where you would use AXIOS to call your Spring Boot endpoint:
        // const response = await axios.get(`${API_BASE_URL}/crowdsourced-data/${authorityName}`);
        // setDashboardData(response.data);
        
        // --- USING MOCK DATA FOR DEMO ---
        await new Promise(resolve => setTimeout(resolve, 500)); // Simulate API delay
        setDashboardData(mockData);
        // ---------------------------------

      } catch (err) {
        console.error("API Fetch Error:", err);
        setError(`Failed to load data for ${authorityName}. Check if the microservice is running.`);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [authorityName]);

  if (loading) {
    return <div className="text-center p-10 text-xl text-blue-600">Loading Dashboard for {authorityName}...</div>;
  }

  if (error) {
    return <div className="bg-red-100 text-red-700 p-4 rounded-lg"><AlertTriangle className="inline w-5 h-5 mr-2" /> Error: {error}</div>;
  }

  return (
    <div>
      <h1 className="text-3xl font-bold text-gray-900 mb-2">💧 {authorityName} Community Dashboard</h1>
      <p className="text-gray-600 mb-8">Detailed water quality observations and citizen ranking.</p>

      {/* 1. Summary Statistics (Requirement i) */}
      <h2 className="text-2xl font-semibold text-gray-800 mb-4">Summary Statistics</h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
        <StatCard 
          title="Total Observations"
          value={dashboardData.totalObservations}
          icon={Database}
          color="#3b82f6" // blue-500
        />
        <StatCard 
          title="Avg. Water pH"
          value={dashboardData.averagePH}
          icon={TrendingUp}
          color="#10b981" // green-500
        />
        <StatCard 
          title="Highest Ranked Citizen"
          value={dashboardData.leaderboard[0].citizenId}
          icon={Users}
          color="#f59e0b" // yellow-500
        />
      </div>

      {/* 2. Recent Observations (Requirement ii) */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-white p-6 rounded-xl shadow-lg">
            <h3 className="text-xl font-semibold mb-4">Five Recent Observations</h3>
            <ul className="divide-y divide-gray-200">
              {dashboardData.recentObservations.map((obs) => (
                <li key={obs.id} className="py-3 flex justify-between items-center">
                  <div className="flex flex-col">
                    <span className="font-medium text-gray-800">{obs.location} (pH: {obs.ph})</span>
                    <span className="text-sm text-gray-500">Date: {obs.date}</span>
                  </div>
                  <span className={`px-2 py-0.5 text-xs font-semibold rounded ${
                      obs.status === 'Poor' ? 'bg-red-100 text-red-800' : obs.status === 'Moderate' ? 'bg-yellow-100 text-yellow-800' : 'bg-green-100 text-green-800'
                  }`}>
                      {obs.status}
                  </span>
                </li>
              ))}
            </ul>
        </div>
        
        {/* 3. Leaderboard (Requirement iii) */}
        <div className="lg:col-span-1 bg-white p-6 rounded-xl shadow-lg">
            <h3 className="text-xl font-semibold mb-4 flex items-center"><Users className="w-5 h-5 mr-2" /> Top Citizens Leaderboard</h3>
            <ol className="space-y-3">
              {dashboardData.leaderboard.map((citizen) => (
                <li key={citizen.rank} className={`flex justify-between items-center p-3 rounded-lg ${citizen.rank === 1 ? 'bg-yellow-50 border-2 border-yellow-500' : 'bg-gray-50'}`}>
                  <div className="flex items-center">
                    <span className={`w-6 h-6 flex items-center justify-center rounded-full text-white font-bold mr-3 ${citizen.rank === 1 ? 'bg-yellow-500' : 'bg-gray-400'}`}>{citizen.rank}</span>
                    <span className="font-medium">{citizen.citizenId}</span>
                  </div>
                  <span className="text-sm font-semibold text-blue-600">{citizen.points} Points</span>
                </li>
              ))}
            </ol>
        </div>
      </div>
    </div>
  );
}