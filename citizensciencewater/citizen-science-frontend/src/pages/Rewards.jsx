// src/pages/Rewards.jsx

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Trophy, Gift, Star } from 'lucide-react';

const API_REWARDS_URL = 'http://localhost:8080/api/v1/rewards'; 

// --- Mock Data ---
const mockRewards = {
  leaderboard: [
    { rank: 1, citizenId: 'A. Smith', observations: 155, points: 3500 },
    { rank: 2, citizenId: 'J. Doe', observations: 120, points: 2800 },
    { rank: 3, citizenId: 'M. Chen', observations: 98, points: 2100 },
    { rank: 4, citizenId: 'R. Patel', observations: 65, points: 1500 },
    { rank: 5, citizenId: 'S. Jones', observations: 42, points: 900 },
  ],
  myRank: 12,
  myPoints: 550,
  badges: [
    { name: 'First Timer', description: 'Submitted first observation.', icon: '⭐', achieved: true },
    { name: 'Water Walker', description: 'Submitted 50 observations.', icon: '🚶', achieved: false },
    { name: 'pH Master', description: 'Submitted 10 observations with full metadata.', icon: '🧪', achieved: true },
  ],
};

export default function Rewards() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // In a real app, use axios.get(API_REWARDS_URL) here
    // For now, simulate loading the mock data
    setTimeout(() => {
      setData(mockRewards);
      setLoading(false);
    }, 500);
  }, []);

  if (loading) {
    return <div className="text-center p-10 text-xl text-yellow-600">Loading Rewards Data...</div>;
  }

  const myCitizenId = 'CS-4001'; // Placeholder for the logged-in user

  return (
    <div>
      <h1 className="text-3xl font-bold text-gray-900 mb-2">🏆 Citizen Rewards & Gamification</h1>
      <p className="text-gray-600 mb-8">See your progress and compete with other citizen scientists.</p>

      {/* My Stats Card */}
      <div className="bg-blue-50 p-6 rounded-xl shadow-md border-2 border-blue-200 mb-10 flex justify-between items-center">
        <div>
          <p className="text-sm font-semibold text-blue-600">My Current Status (Citizen ID: {myCitizenId})</p>
          <p className="text-4xl font-extrabold text-blue-800 mt-1">{data.myPoints} Points</p>
        </div>
        <div className="text-right">
          <p className="text-xl font-bold text-gray-800 flex items-center">Rank: <Trophy className="w-5 h-5 ml-2 text-yellow-500" /> {data.myRank}</p>
        </div>
      </div>
      
      {/* Badges Section */}
      <h2 className="text-2xl font-semibold text-gray-800 mb-4 flex items-center"><Star className="w-6 h-6 mr-2 text-yellow-500" /> Achieved Badges</h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
        {data.badges.map((badge, index) => (
          <div key={index} className={`p-5 rounded-lg shadow-lg transition-transform ${
            badge.achieved ? 'bg-yellow-100 border-l-4 border-yellow-500' : 'bg-gray-100 border-l-4 border-gray-300 opacity-60'
          }`}>
            <span className="text-2xl">{badge.icon}</span>
            <h3 className="text-lg font-bold mt-2">{badge.name}</h3>
            <p className="text-sm text-gray-600">{badge.description}</p>
            {badge.achieved && <p className="text-xs text-green-700 font-semibold mt-1">Achieved!</p>}
          </div>
        ))}
      </div>

      {/* Global Leaderboard */}
      <h2 className="text-2xl font-semibold text-gray-800 mb-4 flex items-center"><Trophy className="w-6 h-6 mr-2 text-blue-500" /> Global Leaderboard</h2>
      <div className="bg-white p-6 rounded-xl shadow-lg">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Rank</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Citizen ID</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Observations</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Points</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {data.leaderboard.map((citizen) => (
              <tr key={citizen.rank} className={citizen.rank <= 3 ? 'bg-yellow-50 font-semibold' : ''}>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{citizen.rank}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{citizen.citizenId}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{citizen.observations}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-blue-600">{citizen.points}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}