import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Trophy, Gift, Star, AlertTriangle, UserPlus, CheckCircle, Droplet, MapPin, Loader2 } from 'lucide-react'; 

// --- API URLs ---
const API_REWARDS_BASE_URL = 'http://localhost:8082/api/rewards'; 
const API_OBSERVATIONS_BASE_URL = 'http://localhost:8090/gateway/observations'; 
const API_ME_URL = 'http://localhost:8083/api/auth/me'; // Endpoint to get current user info

// --- Helper Component: Citizen Observations List (NEW) ---
function CitizenObservations({ citizenId, isAuthReady }) {
    const [observations, setObservations] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!citizenId || !isAuthReady) {
            setObservations([]);
            return;
        }

        const fetchObservations = async () => {
            setLoading(true);
            setError(null);
            try {
                // Construct the full gateway URL for fetching citizen observations
                const url = `${API_OBSERVATIONS_BASE_URL}/citizen/${citizenId}`;
                const token = localStorage.getItem('accessToken');
                
                // Include the Authorization header for robust request handling
                const headers = token ? { 'Authorization': `Bearer ${token}` } : {};

                const response = await axios.get(url, { headers });
                
                // Assuming the response body is an array of observation objects
                setObservations(Array.isArray(response.data) ? response.data : []);
            } catch (err) {
                console.error("Error fetching citizen observations:", err.response ? err.response.data : err.message);
                const errorMsg = err.response?.data?.error || `Failed to load observations: ${err.message}`;
                setError(errorMsg);
            } finally {
                setLoading(false);
            }
        };

        fetchObservations();
    }, [citizenId, isAuthReady]);

    if (!isAuthReady) {
        return <p className="text-center text-gray-500 p-4">Awaiting authentication to load observations...</p>;
    }

    if (loading) {
        return <div className="text-center p-4 flex justify-center items-center text-blue-600"><Loader2 className="w-5 h-5 mr-2 animate-spin" /> Loading Observations...</div>;
    }

    if (error) {
        return <div className="bg-red-100 text-red-700 p-3 rounded-lg border border-red-300"><AlertTriangle className="inline w-4 h-4 mr-2" /> {error}</div>;
    }

    return (
        <div className="mt-4">
            <h3 className="text-xl font-semibold text-gray-800 mb-3 flex items-center"><Droplet className="w-5 h-5 mr-2 text-indigo-500" /> Recent Observations ({observations.length})</h3>
            
            {observations.length === 0 ? (
                <p className="text-center p-4 text-gray-600 bg-gray-50 rounded-lg">No observations found for this citizen ID.</p>
            ) : (
                <div className="space-y-3 max-h-96 overflow-y-auto pr-2 custom-scrollbar">
                    {observations.map((obs, index) => (
                        <div key={index} className="bg-white p-4 rounded-lg shadow-sm border border-gray-100 hover:shadow-md transition">
                            <p className="font-semibold text-blue-600 text-sm truncate">Observation ID: {obs.id}</p>
                            <div className="flex flex-wrap gap-x-4 text-xs text-gray-600 mt-1">
                                <p className="flex items-center"><MapPin className="w-3 h-3 mr-1" /> {obs.postcode}</p>
                                <p className="flex items-center">pH: <span className="font-bold ml-1">{obs.ph}</span></p>
                                <p className="flex items-center">Turbidity: <span className="font-bold ml-1">{obs.turbidity} NTU</span></p>
                            </div>
                            <p className="text-xs text-gray-400 mt-2">Submitted: {new Date(obs.submissionTime).toLocaleString()}</p>
                        </div>
                    ))}
                </div>
            )}
            
            {/* Custom scrollbar class definition for aesthetics */}
            <style jsx="true">{`
                .custom-scrollbar::-webkit-scrollbar {
                    width: 6px;
                }
                .custom-scrollbar::-webkit-scrollbar-track {
                    background: #f1f1f1;
                    border-radius: 10px;
                }
                .custom-scrollbar::-webkit-scrollbar-thumb {
                    background: #ccc;
                    border-radius: 10px;
                }
                .custom-scrollbar::-webkit-scrollbar-thumb:hover {
                    background: #aaa;
                }
            `}</style>
        </div>
    );
}

// --- Helper Component: For creating new Citizen Rewards ---
function CitizenCreator({ onCreated }) {
    const [newCitizenId, setNewCitizenId] = useState('');
    const [status, setStatus] = useState({ message: '', type: '' });
    const [loading, setLoading] = useState(false);

    const handleCreate = async (e) => {
        e.preventDefault();
        if (!newCitizenId.trim()) {
            setStatus({ message: 'Please enter a valid Citizen ID.', type: 'error' });
            return;
        }

        setLoading(true);
        setStatus({ message: '', type: '' });

        try {
            const response = await axios.post(`${API_REWARDS_BASE_URL}/citizen`, {
                citizenId: newCitizenId.trim()
            });

            if (response.status === 201) {
                setStatus({ message: `Successfully created profile for Citizen ID: ${response.data.citizenId}`, type: 'success' });
                setNewCitizenId('');
                if (onCreated) {
                    onCreated(); 
                }
            }
        } catch (err) {
            console.error("Creation Error:", err.response ? err.response.data : err.message);
            const errorMsg = err.response && err.response.data?.error 
                ? err.response.data.error 
                : (err.response && err.response.status === 500) 
                    ? `Error: Profile for ID ${newCitizenId} might already exist or internal server issue.`
                    : `Failed to create profile. Check API server status.`;
            
            setStatus({ message: errorMsg, type: 'error' });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-white p-6 rounded-xl shadow-lg border border-gray-200 mb-10">
            <h3 className="text-xl font-semibold text-gray-800 mb-4 flex items-center">
                <UserPlus className="w-5 h-5 mr-2 text-indigo-500" /> 
                Create New Citizen Reward Profile (Admin Tool)
            </h3>
            <form onSubmit={handleCreate} className="flex flex-col sm:flex-row gap-4">
                <input
                    type="text"
                    placeholder="Enter New Citizen ID (e.g., CS-0005)"
                    value={newCitizenId}
                    onChange={(e) => setNewCitizenId(e.target.value)}
                    className="flex-grow p-3 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500"
                    required
                />
                <button
                    type="submit"
                    disabled={loading}
                    className="w-full sm:w-auto px-6 py-3 bg-indigo-600 text-white font-bold rounded-lg shadow-md hover:bg-indigo-700 transition duration-150 disabled:bg-indigo-400 disabled:cursor-not-allowed"
                >
                    {loading ? 'Creating...' : 'Create Profile'}
                </button>
            </form>
            {status.message && (
                <div className={`mt-4 p-3 rounded-lg flex items-center ${
                    status.type === 'success' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                }`}>
                    {status.type === 'success' ? <CheckCircle className="w-5 h-5 mr-2" /> : <AlertTriangle className="w-5 h-5 mr-2" />}
                    {status.message}
                </div>
            )}
        </div>
    );
}


// --- Main Rewards Component Definition (UPDATED) ---
export default function Rewards() {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [refreshKey, setRefreshKey] = useState(0); 

    // --- Authentication State ---
    const [citizenId, setCitizenId] = useState(null);
    const [isAuth, setIsAuth] = useState(false); 
    const [isReady, setIsReady] = useState(false); 
    
    // 1. Fetch Citizen ID via /me endpoint
    useEffect(() => {
        const fetchCitizenId = async () => {
            setIsReady(false);
            const token = localStorage.getItem('accessToken');
            
            if (!token) {
                setError("No access token found. Please log in to see your rewards.");
                setIsAuth(false);
                setIsReady(true);
                return;
            }

            try {
                const response = await axios.get(API_ME_URL, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });

                const fetchedCitizenId = response.data.citizenId;

                if (fetchedCitizenId) {
                    setCitizenId(fetchedCitizenId);
                    setIsAuth(true);
                    setError(null);
                } else {
                    setError("Authentication error: Citizen ID not found in user data.");
                    setIsAuth(false);
                }
            } catch (err) {
                console.error("Error fetching user data via /me:", err);
                const errorMsg = err.response?.data?.error || "Token invalid or expired. Please sign in again.";
                setError(`Authentication failed: ${errorMsg}`);
                setIsAuth(false);
            } finally {
                setIsReady(true);
            }
        };

        fetchCitizenId();
    }, []); // Run only once for authentication

    // 2. Fetch Rewards data (dependent on successful citizenId retrieval)
    const fetchRewardsData = async () => {
        if (!citizenId) {
            setLoading(false);
            return;
        }

        try {
            setLoading(true);
            setError(null);

            // Execute concurrent API calls
            const leaderboardPromise = axios.get(`${API_REWARDS_BASE_URL}/leaderboard?limit=10`);
            const myRewardPromise = axios.get(`${API_REWARDS_BASE_URL}/citizen/${citizenId}`); 
            const myRankPromise = axios.get(`${API_REWARDS_BASE_URL}/citizen/${citizenId}/rank`);

            const [leaderboardRes, myRewardRes, myRankRes] = await Promise.all([
                leaderboardPromise, 
                myRewardPromise, 
                myRankPromise
            ]);

            // Process Leaderboard data
            const processedLeaderboard = leaderboardRes.data.map((citizen, index) => ({
                rank: index + 1, 
                citizenId: citizen.citizenId,
                observations: citizen.validObservations, 
                points: citizen.totalPoints,
                currentBadge: citizen.currentBadge,
            }));
            
            // Consolidate data
            const consolidatedData = {
                leaderboard: processedLeaderboard,
                myPoints: myRewardRes.data.totalPoints,
                myObservations: myRewardRes.data.validObservations,
                myCurrentBadge: myRewardRes.data.currentBadge,
                myRank: myRankRes.data.rank,

                badges: ['Bronze', 'Silver', 'Gold'].map(level => {
                    const achievedList = myRewardRes.data.badges.split(',').map(s => s.trim()).filter(s => s);
                    const isAchieved = achievedList.includes(level);
                    const description = 
                        level === 'Bronze' ? 'Reached 100 total points.' :
                        level === 'Silver' ? 'Reached 200 total points.' :
                        'Reached 500 total points.';

                    return {
                        name: level,
                        description: description,
                        icon: level === 'Gold' ? '🥇' : level === 'Silver' ? '🥈' : '🥉',
                        achieved: isAchieved,
                    };
                }),
            };

            setData(consolidatedData);
        } catch (err) {
            console.error("Rewards API Fetch Error:", err);
            setError(`Failed to load rewards data. Check if the rewards microservice is running.`);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (isReady && isAuth && citizenId) {
            fetchRewardsData();
        } else if (isReady && !isAuth) {
             setLoading(false);
             setData(null);
        }
    }, [isReady, isAuth, citizenId, refreshKey]); // Rerun fetch when auth state changes or refreshKey changes

    const handleRefresh = () => {
        setRefreshKey(prevKey => prevKey + 1);
    };

    if (!isReady) {
        return <div className="text-center p-10 text-xl text-blue-600 flex justify-center items-center"><Loader2 className="w-6 h-6 mr-2 animate-spin" /> Authenticating user...</div>;
    }

    if (!isAuth || !citizenId) {
        return (
            <div className="p-10 max-w-4xl mx-auto">
                <h1 className="text-3xl font-extrabold text-gray-900 mb-4">🏆 Citizen Rewards & Gamification</h1>
                <div className="bg-red-50 text-red-700 p-6 rounded-xl border border-red-300 shadow-lg text-center">
                    <AlertTriangle className="inline w-6 h-6 mr-2" /> 
                    <p className="font-semibold text-lg mb-2">Authentication Required</p>
                    <p>{error || "Please ensure you are logged in to view your rewards and observations."}</p>
                </div>
            </div>
        );
    }
    
    if (loading && data === null) {
        return <div className="text-center p-10 text-xl text-yellow-600 flex justify-center items-center"><Loader2 className="w-6 h-6 mr-2 animate-spin" /> Loading Rewards Data...</div>;
    }

    // --- Render Dashboard Content ---
    return (
        <div className="p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto">
            <h1 className="text-3xl font-extrabold text-gray-900 mb-2">🏆 Citizen Rewards & Gamification</h1>
            <p className="text-gray-600 mb-8">See your progress and compete with other citizen scientists.</p>
            
            <CitizenCreator onCreated={handleRefresh} />

            {/* My Stats Card & Observations Section */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-10">
                {/* My Stats Card */}
                <div className="lg:col-span-1 bg-blue-50 p-6 rounded-xl shadow-md border-2 border-blue-200">
                    <p className="text-sm font-semibold text-blue-600">My Current Status (Citizen ID: **{citizenId}**)</p>
                    {data ? (
                        <>
                            <p className="text-4xl font-extrabold text-blue-800 mt-1">{data.myPoints} Points</p>
                            <p className="text-lg font-medium text-gray-700">Current Badge: <span className="font-bold text-blue-700">{data.myCurrentBadge}</span></p>
                            <div className="mt-4 text-right">
                                <p className="text-xl font-bold text-gray-800 flex items-center justify-end">Global Rank: <Trophy className="w-5 h-5 ml-2 text-yellow-500" /> **{data.myRank}**</p>
                            </div>
                        </>
                    ) : (
                        <div className="text-center py-6 text-gray-500">Loading stats...</div>
                    )}
                     <button onClick={handleRefresh} className="mt-4 w-full text-sm text-indigo-600 hover:text-indigo-800 transition duration-150 border border-indigo-200 rounded-md p-2">
                            {loading ? 'Refreshing...' : 'Refresh Data'}
                    </button>
                </div>
                
                {/* Citizen Observations List */}
                <div className="lg:col-span-2 bg-gray-50 p-6 rounded-xl shadow-md border-2 border-gray-200">
                    <CitizenObservations citizenId={citizenId} isAuthReady={isReady} />
                </div>
            </div>

            {/* Badges Section */}
            <h2 className="text-2xl font-semibold text-gray-800 mb-4 flex items-center"><Star className="w-6 h-6 mr-2 text-yellow-500" /> Achievements and Badges</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 mb-10">
                {data && data.badges.map((badge, index) => (
                    <div key={index} className={`p-5 rounded-lg shadow-lg transition-transform ${
                        badge.achieved ? 'bg-yellow-100 border-l-4 border-yellow-500 transform hover:scale-[1.02] duration-300' : 'bg-gray-100 border-l-4 border-gray-300 opacity-60'
                    }`}>
                        <span className="text-3xl">{badge.icon}</span>
                        <h3 className="text-lg font-bold mt-2">{badge.name}</h3>
                        <p className="text-sm text-gray-600">{badge.description}</p>
                        {badge.achieved && <p className="text-xs text-green-700 font-semibold mt-1 flex items-center"><CheckCircle className="w-3 h-3 mr-1"/> Achieved!</p>}
                    </div>
                ))}
            </div>

            {/* Global Leaderboard */}
            <h2 className="text-2xl font-semibold text-gray-800 mb-4 flex items-center"><Trophy className="w-6 h-6 mr-2 text-blue-500" /> Global Leaderboard (Top {data?.leaderboard.length || 0})</h2>
            <div className="bg-white p-6 rounded-xl shadow-lg overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Rank</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Citizen ID</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Badge</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Observations</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Points</th>
                        </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                        {data && data.leaderboard.map((citizen) => (
                            <tr key={citizen.citizenId} className={citizen.citizenId === citizenId ? 'bg-blue-50 font-semibold' : citizen.rank <= 3 ? 'bg-yellow-50' : 'hover:bg-gray-50'}>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{citizen.rank}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">**{citizen.citizenId}**</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{citizen.currentBadge}</td>
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
