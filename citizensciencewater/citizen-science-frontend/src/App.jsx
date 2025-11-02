// src/App.jsx (Updated)

import { Routes, Route } from 'react-router-dom';
import Login from './pages/Login.jsx'; 
import DashboardHub from './pages/DashboardHub.jsx'; 
import DashboardLayout from './components/DashboardLayout.jsx'; // 1. Import the Layout
import CommunityDashboard from './pages/CommunityDashboard.jsx'; // Needs to be created next
// You'll need to create the component for submitting data
import SubmitObservation from './pages/SubmitObservation.jsx'; 
import Rewards from './pages/Rewards.jsx'; 
import CitizenRewarder from './pages/CitizenRewarder.jsx';

function App() {
  return (
    <Routes>
      {/* Public Route */}
      <Route path="/" element={<Login />} /> 
      
      {/* Protected Routes using the Layout */}
      <Route element={<DashboardLayout />}>
          {/* Main Hub */}
          <Route path="/dashboard-hub" element={<DashboardHub />} /> 
          
          {/* Dynamic Dashboard Route (The core visualization requirement) */}
          <Route path="/dashboard/:authorityName" element={<CommunityDashboard />} />
          
          {/* Data Submission Page */}
          <Route path="/submit-observation" element={<SubmitObservation />} />

           <Route path="/citizen-rewarder" element={<CitizenRewarder />} /> 
          
          {/* Rewards/Leaderboard Page */}
          <Route path="/rewards" element={<Rewards />} /> 
      </Route>
      
      {/* Catch-all for 404/Unknown routes (optional) */}
      <Route path="*" element={<h1>404 - Not Found</h1>} />
      
    </Routes>
  );
}

export default App;