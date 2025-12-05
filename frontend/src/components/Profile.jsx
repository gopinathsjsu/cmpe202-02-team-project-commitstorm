import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
import { getListings } from '../services/listingsService';
import { getUserMessages } from '../services/messagesService';
import Post from './post';
import Chat from './Chat';
import apiClient from '../services/apiClient';

// Map API listing response to Post component props (same as marketplace)
const mapListingToPostProps = (listing) => {
  let imageUrl = "https://via.placeholder.com/400x200?text=No+Image";
  
  if (listing.images) {
    try {
      let images;
      if (Array.isArray(listing.images)) {
        images = listing.images;
      } else if (typeof listing.images === 'string') {
        const trimmedImages = listing.images.trim();
        if (trimmedImages.startsWith('[') || trimmedImages.startsWith('"')) {
          images = JSON.parse(trimmedImages);
        } else {
          images = [trimmedImages];
        }
      }
      
      if (Array.isArray(images) && images.length > 0) {
        const validImages = images.filter(img => img && typeof img === 'string' && img.trim() !== '');
        if (validImages.length > 0) {
          imageUrl = validImages[0].trim();
        }
      }
    } catch (e) {
      if (typeof listing.images === 'string' && listing.images.trim() !== '') {
        const trimmed = listing.images.trim();
        if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
          imageUrl = trimmed;
        }
      }
    }
  }

  const numericId = listing.id ? parseInt(listing.id.replace(/-/g, '').substring(0, 15), 16) % 1000000 : 0;
  const numericUserId = listing.sellerId ? parseInt(listing.sellerId.replace(/-/g, '').substring(0, 15), 16) % 1000000 : 0;

  return {
    id: numericId,
    userId: numericUserId,
    username: listing.sellerName || 'Unknown Seller',
    description: listing.description || '',
    imageUrl: imageUrl,
    createdAt: listing.createdAt || new Date().toISOString(),
    updatedAt: listing.updatedAt || new Date().toISOString(),
    price: parseFloat(listing.price) || 0,
    condition: listing.condition || 'GOOD',
    category: listing.categoryName || 'Uncategorized',
    title: listing.title || '',
    sellerId: listing.sellerId,
    listingId: listing.id,
  };
};

const Profile = ({ user, onLogout, onLoginClick, onMessageVendor, setUser }) => {
  const navigate = useNavigate();
  const [showMyProfile, setShowMyProfile] = useState(false);
  const [showMyListings, setShowMyListings] = useState(false);
  const [showMyMessages, setShowMyMessages] = useState(false);
  
  const [myListings, setMyListings] = useState([]);
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [messagesLoading, setMessagesLoading] = useState(false);
  const [error, setError] = useState('');
  const [selectedChat, setSelectedChat] = useState(null);
  
  // Profile edit form state
  const [editForm, setEditForm] = useState({
    name: user?.name || '',
    email: user?.email || '',
  });
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState('');

  // Initialize edit form when user changes
  useEffect(() => {
    if (user) {
      setEditForm({
        name: user.name || '',
        email: user.email || '',
      });
    }
  }, [user]);

  // Load user's listings when My Listings is opened
  useEffect(() => {
    if (!user || !showMyListings) return;

    const loadMyListings = async () => {
      setLoading(true);
      setError('');
      try {
        const allListings = await getListings();
        // Filter listings by current user's sellerId
        const userListings = allListings
          .filter((listing) => listing.sellerId === user.id)
          .map(mapListingToPostProps);
        setMyListings(userListings);
      } catch (err) {
        console.error('Error loading listings:', err);
        setError(err.message || 'Failed to load your listings');
      } finally {
        setLoading(false);
      }
    };

    loadMyListings();
  }, [user, showMyListings]);

  // Load user's messages when View My Messages is opened
  useEffect(() => {
    if (!user || !showMyMessages) return;

    const loadMessages = async () => {
      setMessagesLoading(true);
      try {
        const messagesData = await getUserMessages(user.id, 0, 100);
        
        // Handle paginated response
        const messageList = Array.isArray(messagesData) 
          ? messagesData 
          : (messagesData.content || messagesData);
        
        setMessages(messageList || []);
      } catch (err) {
        console.error('Error loading messages:', err);
        setError(err.message || 'Failed to load messages');
      } finally {
        setMessagesLoading(false);
      }
    };

    loadMessages();
    // Removed polling - messages will load when component mounts or user changes
  }, [user, showMyMessages]);

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setSaving(true);
    setSaveError('');

    try {
      // Update user profile via API (assuming endpoint exists)
      const response = await apiClient.put(`/api/users/${user.id}`, {
        name: editForm.name,
        email: editForm.email,
      });

      // Update local storage and state
      const updatedUser = {
        ...user,
        name: editForm.name,
        email: editForm.email,
      };
      localStorage.setItem('auth.user', JSON.stringify(updatedUser));
      if (setUser) {
        setUser(updatedUser);
      }
      
      alert('Profile updated successfully!');
      setShowMyProfile(false);
    } catch (err) {
      console.error('Error updating profile:', err);
      setSaveError(err.message || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  const formatTime = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;
    const minutes = Math.floor(diff / 60000);
    
    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (minutes < 1440) return `${Math.floor(minutes / 60)}h ago`;
    return date.toLocaleDateString();
  };

  if (!user) {
    return (
      <div className="min-h-screen bg-gray-900 text-white flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold mb-4">Please log in to view your profile</h2>
          <button
            onClick={onLoginClick}
            className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-lg font-medium"
          >
            Log In
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Profile Header */}
        <div className="bg-gray-800 rounded-lg p-6 mb-6">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-3xl font-bold mb-2">{user.name || user.email}</h1>
              <p className="text-gray-400">{user.email}</p>
            </div>
            <button
              onClick={onLogout}
              className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg font-medium"
            >
              Logout
            </button>
          </div>
        </div>

        {/* Three Main Sections */}
        <div className="space-y-4">
          {/* My Profile Section */}
          <div className="bg-gray-800 rounded-lg overflow-hidden">
            <button
              onClick={() => setShowMyProfile(!showMyProfile)}
              className="w-full px-6 py-4 text-left flex justify-between items-center hover:bg-gray-700 transition-colors"
            >
              <span className="text-lg font-semibold">My Profile</span>
              <svg
                className={`w-5 h-5 transition-transform ${showMyProfile ? 'rotate-180' : ''}`}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </button>
            
            {showMyProfile && (
              <div className="px-6 py-4 border-t border-gray-700">
                <form onSubmit={handleSaveProfile} className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-2">
                      Name
                    </label>
                    <input
                      type="text"
                      value={editForm.name}
                      onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                      className="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
                      required
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-2">
                      Email
                    </label>
                    <input
                      type="email"
                      value={editForm.email}
                      onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                      className="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
                      required
                    />
                  </div>

                  {saveError && (
                    <div className="text-red-400 text-sm">{saveError}</div>
                  )}

                  <button
                    type="submit"
                    disabled={saving}
                    className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2 rounded-lg font-medium disabled:opacity-50"
                  >
                    {saving ? 'Saving...' : 'Save Changes'}
                  </button>
                </form>
              </div>
            )}
          </div>

          {/* My Listings Section */}
          <div className="bg-gray-800 rounded-lg overflow-hidden">
            <button
              onClick={() => setShowMyListings(!showMyListings)}
              className="w-full px-6 py-4 text-left flex justify-between items-center hover:bg-gray-700 transition-colors"
            >
              <span className="text-lg font-semibold">My Listings</span>
              <svg
                className={`w-5 h-5 transition-transform ${showMyListings ? 'rotate-180' : ''}`}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </button>
            
            {showMyListings && (
              <div className="px-6 py-4 border-t border-gray-700">
                {loading ? (
                  <div className="text-center py-8">
                    <div className="text-gray-400">Loading your listings...</div>
                  </div>
                ) : error ? (
                  <div className="text-center py-8 text-red-400">{error}</div>
                ) : myListings.length === 0 ? (
                  <div className="text-center py-8">
                    <p className="text-gray-400 mb-4">You haven't created any listings yet.</p>
                    <button
                      onClick={() => navigate('/marketplace')}
                      className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-lg font-medium"
                    >
                      Browse Marketplace
                    </button>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {myListings.map((listing) => (
                      <Post key={listing.listingId || listing.id} {...listing} />
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>

          {/* View My Messages Section */}
          <div className="bg-gray-800 rounded-lg overflow-hidden">
            <button
              onClick={() => setShowMyMessages(!showMyMessages)}
              className="w-full px-6 py-4 text-left flex justify-between items-center hover:bg-gray-700 transition-colors"
            >
              <span className="text-lg font-semibold">View My Messages</span>
              <svg
                className={`w-5 h-5 transition-transform ${showMyMessages ? 'rotate-180' : ''}`}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </button>
            
            {showMyMessages && (
              <div className="px-6 py-4 border-t border-gray-700">
                {messagesLoading ? (
                  <div className="text-center py-8">
                    <div className="text-gray-400">Loading messages...</div>
                  </div>
                ) : error ? (
                  <div className="text-center py-8 text-red-400">{error}</div>
                ) : messages.length === 0 ? (
                  <div className="text-center py-8">
                    <p className="text-gray-400">You have no messages yet.</p>
                  </div>
                ) : (
                  <div className="space-y-3 max-h-96 overflow-y-auto">
                    {messages.map((msg) => {
                      const isSent = (msg.senderId || msg.fromUserId) === user.id;
                      const partnerId = isSent 
                        ? (msg.recipientId || msg.toUserId)
                        : (msg.senderId || msg.fromUserId);
                      const partnerName = isSent
                        ? (msg.recipientName || msg.toUserName || 'Unknown')
                        : (msg.senderName || msg.fromUserName || 'Unknown');

                      return (
                        <div
                          key={msg.id || msg.messageId}
                          onClick={() => {
                            if (msg.listingId) {
                              setSelectedChat({
                                listingId: msg.listingId,
                                recipientId: partnerId,
                                recipientName: partnerName,
                                listingTitle: msg.listingTitle || msg.title || 'Listing',
                              });
                            }
                          }}
                          className="bg-gray-700 rounded-lg p-4 cursor-pointer hover:bg-gray-600 transition-colors"
                        >
                          <div className="flex justify-between items-start mb-2">
                            <div className="flex-1">
                              <div className="flex items-center gap-2 mb-1">
                                <span className="font-semibold">{partnerName}</span>
                                <span className={`text-xs px-2 py-1 rounded ${
                                  isSent 
                                    ? 'bg-blue-600 text-white' 
                                    : 'bg-green-600 text-white'
                                }`}>
                                  {isSent ? 'Sent' : 'Received'}
                                </span>
                              </div>
                              {msg.listingTitle && (
                                <p className="text-sm text-gray-400 mb-1">
                                  Listing: {msg.listingTitle}
                                </p>
                              )}
                              <p className="text-gray-300">{msg.content}</p>
                            </div>
                            <div className="ml-4 text-right">
                              <p className="text-xs text-gray-500">
                                {formatTime(msg.createdAt || msg.timestamp)}
                              </p>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Chat Modal */}
      {selectedChat && (
        <Chat
          listingId={selectedChat.listingId}
          recipientId={selectedChat.recipientId}
          recipientName={selectedChat.recipientName}
          listingTitle={selectedChat.listingTitle}
          currentUserId={user.id}
          onClose={() => setSelectedChat(null)}
        />
      )}
    </div>
  );
};

export default Profile;
