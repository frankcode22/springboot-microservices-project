// src/components/DashboardLayout.jsx

import React from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { Home, Zap, Users, LogOut, Upload } from 'lucide-react'; 
// Install Lucide Icons for React: npm install lucide-react

// Define the menu links for the sidebar
const sidebarLinks = [
  { name: 'Dashboard Hub', path: '/dashboard-hub', icon: Home },
  { name: 'My Community Dashboard', path: '/dashboard/MyAuthority', icon: Users }, // Replace 'MyAuthority' with a dynamic value later
  { name: 'Submit Observation', path: '/submit-observation', icon: Upload },
   { name: 'Reward and Points', path: '/citizen-rewarder', icon: Zap },
  { name: 'Rewards & Leaderboard', path: '/rewards', icon: Zap },
];

function Sidebar() {
  const navigate = useNavigate();

  const handleLogout = () => {
    // Clear authentication data
    localStorage.removeItem('accessToken');
    localStorage.removeItem('user');
    // Redirect to the login page
    navigate('/');
  };
  
  // Placeholder for user info (will fetch from context/localStorage in a real app)
  const user = JSON.parse(localStorage.getItem('user')) || { username: 'Citizen Scientist', roles: ['USER'] };

  return (
    <div className="flex flex-col w-64 bg-gray-800 text-white h-screen fixed">
      
      {/* Logo/Title */}
      <div className="p-6 text-xl font-bold border-b border-gray-700">
        💧 Water Quality App
      </div>

      {/* Navigation Links */}
      <nav className="flex-grow p-4 space-y-2">
        {sidebarLinks.map((link) => (
          <NavLink
            key={link.name}
            to={link.path}
            className={({ isActive }) =>
              `flex items-center p-3 rounded-lg transition-colors duration-200 ${
                isActive
                  ? 'bg-blue-600 text-white font-semibold'
                  : 'text-gray-300 hover:bg-gray-700 hover:text-white'
              }`
            }
          >
            <link.icon className="w-5 h-5 mr-3" />
            {link.name}
          </NavLink>
        ))}
      </nav>

      {/* User and Logout Section */}
      <div className="p-4 border-t border-gray-700 space-y-2">
        <div className="text-sm">
          Welcome, **{user.username}**
        </div>
        <button
          onClick={handleLogout}
          className="w-full flex items-center justify-center p-2 rounded-lg bg-red-600 hover:bg-red-700 transition-colors text-white"
        >
          <LogOut className="w-4 h-4 mr-2" />
          Logout
        </button>
      </div>
    </div>
  );
}

export default function DashboardLayout() {
  return (
    <div className="flex min-h-screen">
      <Sidebar />
      
      {/* Main Content Area: Padding matches sidebar width */}
      <main className="flex-1 ml-64 p-8 bg-gray-50">
        {/* The Outlet renders the specific component for the current route (e.g., DashboardHub, CommunityDashboard) */}
        <Outlet /> 
      </main>
    </div>
  );
}