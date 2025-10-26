import { useState, useEffect } from 'react'
import {BrowserRouter, Route, Routes} from 'react-router';
import './css/App.css'
import Header from './components/header'
import Home from './components/home'
import { Marketplace } from './components/marketplace';
import Login from './components/auth/Login';
import Signup from './components/auth/Signup';
import { setAuthToken, clearAuthToken } from './services/apiClient';

function App() {
  const [showAuth, setShowAuth] = useState(null); // null, 'login', or 'signup'
  const [user, setUser] = useState(null);

  // Check for existing auth on app load
  useEffect(() => {
    const token = localStorage.getItem('auth.token');
    const userData = localStorage.getItem('auth.user');
    
    if (token && userData) {
      setUser(JSON.parse(userData));
      // Set the token in the API client
      setAuthToken(token);
    }
  }, []);

  // Handle token expiration
  useEffect(() => {
    const handleTokenExpired = () => {
      setUser(null);
      setShowAuth('login');
      console.log('Token expired, please log in again');
    };

    window.addEventListener('auth:tokenExpired', handleTokenExpired);
    return () => window.removeEventListener('auth:tokenExpired', handleTokenExpired);
  }, []);

  const handleAuthSuccess = (userData) => {
    setUser(userData);
    setShowAuth(null); // Close auth modal
    console.log('Authentication successful:', userData);
  };

  const handleLogout = () => {
    clearAuthToken();
    localStorage.removeItem('auth.user');
    setUser(null);
    console.log('Logged out');
  };

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      <Header 
        user={user} 
        onLoginClick={() => setShowAuth('login')} 
        onSignupClick={() => setShowAuth('signup')}
        onLogout={handleLogout}
      />
      
      {/* Auth Modal Overlay */}
      {showAuth && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="relative">
            <button
              onClick={() => setShowAuth(null)}
              className="absolute top-4 right-4 text-gray-400 hover:text-white text-2xl"
            >
              ×
            </button>
            {showAuth === 'login' && (
              <Login onAuthSuccess={handleAuthSuccess} onClose={() => setShowAuth(null)} />
            )}
            {showAuth === 'signup' && (
              <Signup onAuthSuccess={handleAuthSuccess} onClose={() => setShowAuth(null)} />
            )}
          </div>
        </div>
      )}
      
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Home />}/>
          <Route path="/marketplace" element={<Marketplace/>}/>
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App