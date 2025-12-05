import { useState, useEffect } from 'react'
import {BrowserRouter, Route, Routes, useNavigate, useLocation} from 'react-router';
import './css/App.css'
import Header from './components/header'
import Home from './components/home'
import { Marketplace } from './components/marketplace';
import { MyProfileModal, MyListingsModal, MyMessagesModal, ManageReportsModal } from './components/ProfileModals';
import Login from './components/auth/Login';
import Signup from './components/auth/Signup';
import ListingForm from './components/listingForm';
import Chat from './components/Chat';
import ReportModal from './components/ReportModal';
import EditListingModal from './components/EditListingModal';
import DeleteListingModal from './components/DeleteListingModal';
import { setAuthToken, clearAuthToken } from './services/apiClient';
import { logout as logoutAPI } from './services/authService';
import { updateListingStatus } from './services/listingsService';

function AppContent() {
  const navigate = useNavigate();
  const location = useLocation();
  const [showAuth, setShowAuth] = useState(null); // null, 'login', or 'signup'
  const [user, setUser] = useState(null);
  const [chatData, setChatData] = useState(null); // { listingId, recipientId, recipientName, listingTitle }
  const [showProfileModal, setShowProfileModal] = useState(null); // null, 'profile', 'listings', 'messages'
  const [reportData, setReportData] = useState(null); // { listingId, listingTitle }
  const [editListingId, setEditListingId] = useState(null); // listingId to edit in modal
  const [deleteListingData, setDeleteListingData] = useState(null); // { listingId, listingTitle }
  const [listingsRefreshTrigger, setListingsRefreshTrigger] = useState(0); // Trigger to refresh My Listings

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

  const handleAuthSuccess = async (userData) => {
    setUser(userData);
    setShowAuth(null); // Close auth modal
    console.log('Authentication successful:', userData);
    // Redirect to marketplace on successful login
    navigate('/marketplace');
  };

  const handleLogout = async () => {
    try {
      // Call logout API
      await logoutAPI();
    } catch (error) {
      console.error('Logout API error:', error);
    } finally {
      // Clear local auth state regardless of API call result
      clearAuthToken();
      localStorage.removeItem('auth.user');
      localStorage.removeItem('auth.tokenType');
      setUser(null);
      console.log('Logged out');
      // Redirect to home page
      navigate('/');
    }
  };

  const handleEditListing = (listingId) => {
    console.log('handleEditListing called with:', listingId);
    if (!listingId) {
      console.error('No listingId provided to handleEditListing');
      alert('Error: Listing ID is missing');
      return;
    }
    // Open edit listing modal
    setEditListingId(listingId);
  };

  const handleEditSuccess = () => {
    // Trigger refresh of My Listings modal if it's open
    setListingsRefreshTrigger(prev => prev + 1);
    // Also refresh marketplace if we're on that page
    if (location.pathname === '/marketplace') {
      window.location.reload();
    }
  };

  const handleDeleteListing = (listingId, listingTitle) => {
    console.log('handleDeleteListing called with:', listingId, listingTitle);
    if (!listingId) {
      console.error('No listingId provided to handleDeleteListing');
      alert('Error: Listing ID is missing');
      return;
    }
    // Open delete confirmation modal
    setDeleteListingData({
      listingId: listingId,
      listingTitle: listingTitle || 'this listing'
    });
  };

  const handleDeleteSuccess = () => {
    // Trigger refresh of My Listings modal if it's open
    setListingsRefreshTrigger(prev => prev + 1);
    // Also refresh marketplace if we're on that page
    if (location.pathname === '/marketplace') {
      window.location.reload();
    }
  };

  const handleMarkAsSold = async (listingId) => {
    console.log('handleMarkAsSold called with:', listingId);
    if (!listingId) {
      console.error('No listingId provided to handleMarkAsSold');
      alert('Error: Listing ID is missing');
      return;
    }
    try {
      console.log('Marking listing as sold:', listingId);
      await updateListingStatus(listingId, 'SOLD');
      console.log('Listing marked as sold successfully');
      alert('Listing marked as sold successfully!');
      
      // Trigger refresh of My Listings modal if it's open
      setListingsRefreshTrigger(prev => prev + 1);
      // Also refresh marketplace if we're on that page
      if (location.pathname === '/marketplace') {
        window.location.reload();
      }
    } catch (error) {
      console.error('Error marking listing as sold:', error);
      alert(error.message || 'Failed to mark listing as sold. Please try again.');
    }
  };

  // Check if we're on home route (public)
  const isHome = location.pathname === '/';

  // Protect routes - redirect to home if not authenticated
  // Note: /marketplace is now a public route
  useEffect(() => {
    const publicRoutes = ['/', '/marketplace'];
    const isProtectedRoute = !publicRoutes.includes(location.pathname);
    const token = localStorage.getItem('auth.token');
    if (isProtectedRoute && !user && !token) {
      navigate('/');
    }
  }, [location.pathname, user, navigate]);

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      <Header 
        user={user} 
        isHome={isHome}
        onLoginClick={() => setShowAuth('login')} 
        onSignupClick={() => setShowAuth('signup')}
        onLogout={handleLogout}
        onMyProfileClick={() => setShowProfileModal('profile')}
        onMyListingsClick={() => setShowProfileModal('listings')}
        onMyMessagesClick={() => setShowProfileModal('messages')}
        onReportsClick={() => setShowProfileModal('reports')}
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
      
      <Routes>
        <Route index element={<Home />}/>
        <Route path="marketplace" element={<Marketplace onMessageVendor={setChatData} onReportPost={setReportData} onEditListing={handleEditListing} onDeleteListing={handleDeleteListing}/>}/>
        <Route path="create-listing" element={<ListingForm/>}/>
      </Routes>

      {/* Profile Modals */}
      {showProfileModal === 'profile' && user && (
        <MyProfileModal
          user={user}
          onClose={() => setShowProfileModal(null)}
          setUser={setUser}
        />
      )}
      {showProfileModal === 'listings' && user && (
        <MyListingsModal
          user={user}
          onClose={() => setShowProfileModal(null)}
          onEditListing={handleEditListing}
          onDeleteListing={handleDeleteListing}
          onMarkAsSold={handleMarkAsSold}
          refreshTrigger={listingsRefreshTrigger}
        />
      )}
      {showProfileModal === 'messages' && user && (
        <MyMessagesModal
          user={user}
          onClose={() => setShowProfileModal(null)}
          onMessageVendor={setChatData}
        />
      )}
      {showProfileModal === 'reports' && user && (
        <ManageReportsModal
          user={user}
          onClose={() => setShowProfileModal(null)}
        />
      )}

      {/* Chat Modal */}
      {chatData && user && (
        <Chat
          listingId={chatData.listingId}
          recipientId={chatData.recipientId}
          recipientName={chatData.recipientName}
          listingTitle={chatData.listingTitle}
          currentUserId={user.id}
          onClose={() => setChatData(null)}
        />
      )}

      {/* Report Modal */}
      {reportData && user && (
        <ReportModal
          listingId={reportData.listingId}
          listingTitle={reportData.listingTitle}
          reporterId={user.id}
          onClose={() => setReportData(null)}
        />
      )}

      {/* Edit Listing Modal */}
      {editListingId && (
        <EditListingModal
          listingId={editListingId}
          onClose={() => setEditListingId(null)}
          onSuccess={handleEditSuccess}
        />
      )}

      {/* Delete Listing Modal */}
      {deleteListingData && (
        <DeleteListingModal
          listingId={deleteListingData.listingId}
          listingTitle={deleteListingData.listingTitle}
          onClose={() => setDeleteListingData(null)}
          onSuccess={handleDeleteSuccess}
        />
      )}
    </div>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  );
}

export default App