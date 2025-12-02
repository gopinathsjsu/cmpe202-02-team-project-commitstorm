import { useState, useEffect } from 'react';
import { getListings } from '../services/listingsService';
import { getUserMessages } from '../services/messagesService';
import Post from './post';
import Chat from './Chat';
import Loader from './Loader';
import apiClient from '../services/apiClient';

// Map API listing response to Post component props
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

// My Profile Modal
export const MyProfileModal = ({ user, onClose, setUser }) => {
  const [editForm, setEditForm] = useState({
    name: user?.name || '',
    email: user?.email || '',
  });
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState('');

  useEffect(() => {
    if (user) {
      setEditForm({
        name: user.name || '',
        email: user.email || '',
      });
    }
  }, [user]);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setSaveError('');

    try {
      const response = await apiClient.put(`/api/users/${user.id}`, {
        name: editForm.name,
        email: editForm.email,
      });

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
      onClose();
    } catch (err) {
      console.error('Error updating profile:', err);
      setSaveError(err.message || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-gray-800 rounded-lg shadow-xl w-full max-w-md p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-2xl font-bold text-white">My Profile</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-white text-2xl font-bold"
          >
            ×
          </button>
        </div>

        <form onSubmit={handleSave} className="space-y-4">
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

          <div className="flex gap-3">
            <button
              type="submit"
              disabled={saving}
              className="flex-1 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg font-medium disabled:opacity-50"
            >
              {saving ? 'Saving...' : 'Save Changes'}
            </button>
            <button
              type="button"
              onClick={onClose}
              className="flex-1 bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-lg font-medium"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// My Listings Modal
export const MyListingsModal = ({ user, onClose }) => {
  const [myListings, setMyListings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user) return;

    const loadMyListings = async () => {
      setLoading(true);
      setError('');
      try {
        const allListings = await getListings();
        // Filter by user ID - compare both as strings to handle UUID format
        const userListings = allListings
          .filter((listing) => {
            // Compare sellerId with user.id (both should be UUID strings)
            return String(listing.sellerId) === String(user.id);
          })
          .map(mapListingToPostProps);
        console.log('My listings filtered:', {
          userId: user.id,
          totalListings: allListings.length,
          myListings: userListings.length,
          sampleListing: allListings[0]?.sellerId
        });
        setMyListings(userListings);
      } catch (err) {
        console.error('Error loading listings:', err);
        setError(err.message || 'Failed to load your listings');
      } finally {
        setLoading(false);
      }
    };

    loadMyListings();
  }, [user]);

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-gray-800 rounded-lg shadow-xl w-full max-w-6xl max-h-[90vh] flex flex-col">
        <div className="flex justify-between items-center p-6 border-b border-gray-700">
          <h2 className="text-2xl font-bold text-white">My Listings</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-white text-2xl font-bold"
          >
            ×
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-6">
          {loading ? (
            <Loader text="Loading your listings..." />
          ) : error ? (
            <div className="text-center py-12 text-red-400">{error}</div>
          ) : myListings.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-gray-400">You haven't created any listings yet.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {myListings.map((listing) => (
                <Post key={listing.listingId || listing.id} {...listing} />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

// My Messages Modal
export const MyMessagesModal = ({ user, onClose, onMessageVendor }) => {
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedChat, setSelectedChat] = useState(null);

  useEffect(() => {
    if (!user) return;

    const loadMessages = async () => {
      setLoading(true);
      try {
        const messagesData = await getUserMessages(user.id, 0, 100);
        const messageList = Array.isArray(messagesData) 
          ? messagesData 
          : (messagesData.content || messagesData);
        setMessages(messageList || []);
      } catch (err) {
        console.error('Error loading messages:', err);
        setError(err.message || 'Failed to load messages');
      } finally {
        setLoading(false);
      }
    };

    loadMessages();
    const interval = setInterval(loadMessages, 5000);
    return () => clearInterval(interval);
  }, [user]);

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

  return (
    <>
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-gray-800 rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] flex flex-col">
          <div className="flex justify-between items-center p-6 border-b border-gray-700">
            <h2 className="text-2xl font-bold text-white">View My Messages</h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-white text-2xl font-bold"
            >
              ×
            </button>
          </div>

          <div className="flex-1 overflow-y-auto p-6">
            {loading ? (
              <Loader text="Loading messages..." />
            ) : error ? (
              <div className="text-center py-12 text-red-400">{error}</div>
            ) : messages.length === 0 ? (
              <div className="text-center py-12">
                <p className="text-gray-400">You have no messages yet.</p>
              </div>
            ) : (
              <div className="space-y-3">
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
    </>
  );
};

