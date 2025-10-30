// src/pages/DashboardHub.jsx

import React from 'react';
import { BarChart, MapPin, Award, Activity } from 'lucide-react';
import { Link } from 'react-router-dom';

// Placeholder data representing the high-level metrics for the HUB
const hubMetrics = [
  {
    title: 'Total Observations Recorded',
    value: '15,482',
    icon: Activity,
    color: 'bg-blue-500',
    description: 'Across all local authorities.',
  },
  {
    title: 'Active Communities',
    value: '4 / 4',
    icon: MapPin,
    color: 'bg-green-500',
    description: 'All group microservices are online.',
  },
  {
    title: 'Top Citizen Scientist',
    value: 'A. Smith',
    icon: Award,
    color: 'bg-yellow-500',
    description: 'View the latest monthly leaderboard.',
  },
  {
    title: 'Data Visualizations',
    value: '3+ Types',
    icon: BarChart,
    color: 'bg-purple-500',
    description: 'Bar, Line, and Map Charts available.',
  },
];

const CommunityCard = ({ name, path, status }) => (
    <Link to={path} className="block bg-white p-6 rounded-lg shadow-md hover:shadow-xl transition-shadow duration-300 border-t-4 border-blue-500">
        <h3 className="text-xl font-bold text-gray-800 mb-2">{name}</h3>
        <p className="text-gray-600 mb-3">View the detailed dashboard for this local authority.</p>
        <div className={`text-sm font-semibold p-1 rounded inline-block ${status === 'Active' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
            Status: {status}
        </div>
    </Link>
);


export default function DashboardHub() {
  return (
    <div>
      <h1 className="text-3xl font-bold text-gray-900 mb-6">🌎 Citizen Science Dashboard Hub</h1>
      <p className="text-lg text-gray-600 mb-8">
        Welcome! This hub provides a high-level overview and links to the detailed community dashboards managed by your team's microservices.
      </p>

      {/* --- Section 1: Key Metrics (Assignment Overview) --- */}
      <h2 className="text-2xl font-semibold text-gray-800 mb-4 border-b pb-2">Key Assessment Progress</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
        {hubMetrics.map((metric, index) => (
          <div key={index} className="bg-white p-6 rounded-xl shadow-lg transition transform hover:scale-[1.02]">
            <metric.icon className={`w-8 h-8 ${metric.color.replace('bg-', 'text-')} mb-3`} />
            <p className="text-sm font-medium text-gray-500">{metric.title}</p>
            <p className="text-3xl font-extrabold text-gray-900 mt-1">{metric.value}</p>
            <p className="text-xs text-gray-400 mt-2">{metric.description}</p>
          </div>
        ))}
      </div>

      {/* --- Section 2: Community Dashboard Links (Modular Component Access) --- */}
      <h2 className="text-2xl font-semibold text-gray-800 mb-4 border-b pb-2">Local Authority Dashboards (Group Microservices)</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Placeholder links for each group member's microservice integration */}
        <CommunityCard 
            name="Community A: Crowdsourced Data"
            path="/dashboard/CommunityA" 
            status="Active" 
        />
        <CommunityCard 
            name="Community B: Rewards Service"
            path="/dashboard/CommunityB" 
            status="Active" 
        />
        <CommunityCard 
            name="Community C: Citizen ID Service"
            path="/dashboard/CommunityC" 
            status="Active" 
        />
        <CommunityCard 
            name="Community D: Placeholder / Future Service"
            path="/dashboard/CommunityD" 
            status="Inactive" 
        />
      </div>

      {/* Note: The actual data fetching for metrics would happen here using axios and useEffect */}

    </div>
  );
}